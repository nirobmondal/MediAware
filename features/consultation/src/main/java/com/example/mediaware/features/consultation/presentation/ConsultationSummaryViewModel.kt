package com.example.mediaware.features.consultation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.database.dao.ConsultationDao
import com.example.mediaware.core.database.entity.ConsultationEntity
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.consultation.domain.model.ActionItemCategory
import com.example.mediaware.features.consultation.domain.model.ConsultationActionItem
import com.example.mediaware.features.consultation.domain.model.ConsultationSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

data class ConsultationSummaryUiState(
    val isLoading: Boolean = true,
    val consultations: List<ConsultationSummary> = emptyList(),
    val expandedConsultationId: String? = null,
    val selectedCategory: ActionItemCategory? = null,
    val scheduledCalendarIds: Set<String> = emptySet(),
    val toastMessage: String? = null
)

sealed interface ConsultationSummaryUiEvent {
    data class OnToggleExpand(val consultationId: String) : ConsultationSummaryUiEvent
    data class OnToggleActionItem(val consultationId: String, val itemId: String) : ConsultationSummaryUiEvent
    data class OnSelectCategory(val category: ActionItemCategory?) : ConsultationSummaryUiEvent
    data class OnScheduleCalendar(val consultationId: String) : ConsultationSummaryUiEvent
    data class OnDeleteConsultation(val consultationId: String) : ConsultationSummaryUiEvent
    data object OnDismissToast : ConsultationSummaryUiEvent
}

sealed interface ConsultationSummarySideEffect {
    data class LaunchCalendarIntent(
        val title: String,
        val description: String,
        val startEpochMillis: Long
    ) : ConsultationSummarySideEffect
    data class ShowToast(val message: String) : ConsultationSummarySideEffect
}

@HiltViewModel
class ConsultationSummaryViewModel @Inject constructor(
    private val consultationDao: ConsultationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsultationSummaryUiState())
    val uiState: StateFlow<ConsultationSummaryUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ConsultationSummarySideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            consultationDao.getAllConsultationsFlow().collect { entities ->
                val summaries = entities.map { it.toDomainSummary() }
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        consultations = summaries,
                        expandedConsultationId = current.expandedConsultationId ?: summaries.firstOrNull()?.id
                    )
                }
            }
        }
    }

    fun onEvent(event: ConsultationSummaryUiEvent) {
        when (event) {
            is ConsultationSummaryUiEvent.OnToggleExpand -> {
                _uiState.update {
                    val next = if (it.expandedConsultationId == event.consultationId) null else event.consultationId
                    it.copy(expandedConsultationId = next)
                }
            }
            is ConsultationSummaryUiEvent.OnToggleActionItem -> {
                toggleActionItem(event.consultationId, event.itemId)
            }
            is ConsultationSummaryUiEvent.OnSelectCategory -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
            }
            is ConsultationSummaryUiEvent.OnScheduleCalendar -> {
                scheduleCalendar(event.consultationId)
            }
            is ConsultationSummaryUiEvent.OnDeleteConsultation -> {
                deleteConsultation(event.consultationId)
            }
            ConsultationSummaryUiEvent.OnDismissToast -> {
                _uiState.update { it.copy(toastMessage = null) }
            }
        }
    }

    private fun toggleActionItem(consultationId: String, itemId: String) {
        viewModelScope.launch {
            val entity = consultationDao.getConsultationById(consultationId) ?: return@launch
            try {
                val arr = JSONArray(entity.actionItemsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    if (obj.optString("id") == itemId) {
                        val current = obj.optBoolean("isCompleted", false)
                        obj.put("isCompleted", !current)
                        break
                    }
                }
                val updated = entity.copy(actionItemsJson = arr.toString())
                consultationDao.updateConsultation(updated)
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }
    }

    private fun scheduleCalendar(consultationId: String) {
        val currentSummary = _uiState.value.consultations.find { it.id == consultationId } ?: return
        val followUpMillis = System.currentTimeMillis() + (currentSummary.followUpDays * 24L * 60L * 60L * 1000L)
        val title = "ডাক্তার ফলো-আপ ভিজিট: ${currentSummary.doctorName}"
        val description = "ফলো-আপ কারণ: ${currentSummary.followUpReasonBn}\n\nকরণীয়:\n" +
                currentSummary.actionItems.joinToString("\n") { "• ${it.task}" }

        _uiState.update {
            it.copy(scheduledCalendarIds = it.scheduledCalendarIds + consultationId)
        }

        viewModelScope.launch {
            _sideEffect.send(
                ConsultationSummarySideEffect.LaunchCalendarIntent(
                    title = title,
                    description = description,
                    startEpochMillis = followUpMillis
                )
            )
            _sideEffect.send(
                ConsultationSummarySideEffect.ShowToast(
                    "✅ ${currentSummary.followUpDays.toString().toBengaliDigits()} দিন পরের ফলো-আপ রিমাইন্ডার ক্যালেন্ডারে সেট করা হয়েছে।"
                )
            )
        }
    }

    private fun deleteConsultation(consultationId: String) {
        viewModelScope.launch {
            consultationDao.deleteConsultationById(consultationId)
            _sideEffect.send(ConsultationSummarySideEffect.ShowToast("পরামর্শের রেকর্ড সফলভাবে মুছে ফেলা হয়েছে।"))
        }
    }

    private fun ConsultationEntity.toDomainSummary(): ConsultationSummary {
        val items = mutableListOf<ConsultationActionItem>()
        try {
            val arr = JSONArray(actionItemsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val catStr = obj.optString("category", "GENERAL").uppercase()
                val cat = when (catStr) {
                    "MEDICATION" -> ActionItemCategory.MEDICATION
                    "TEST" -> ActionItemCategory.TEST
                    "LIFESTYLE" -> ActionItemCategory.LIFESTYLE
                    else -> ActionItemCategory.GENERAL
                }
                items.add(
                    ConsultationActionItem(
                        id = obj.optString("id", "act_$i"),
                        task = obj.optString("task", ""),
                        category = cat,
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore JSON decode error
        }

        val questions = mutableListOf<String>()
        try {
            val qArr = JSONArray(pendingQuestionsJson)
            for (i in 0 until qArr.length()) {
                questions.add(qArr.getString(i))
            }
        } catch (e: Exception) {
            // Ignore JSON decode error
        }

        return ConsultationSummary(
            id = id,
            doctorName = doctorName,
            visitDateBn = dateFormattedBn,
            summaryBn = summaryBn,
            actionItems = items,
            pendingQuestions = questions,
            followUpDays = followUpDays,
            followUpDateStringBn = followUpDateStringBn,
            followUpReasonBn = followUpReasonBn
        )
    }
}
