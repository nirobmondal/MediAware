package com.example.mediaware.features.consultation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.consultation.domain.model.ActionItemCategory
import com.example.mediaware.features.consultation.domain.model.ConsultationActionItem
import com.example.mediaware.features.consultation.domain.model.ConsultationSummary
import com.example.mediaware.features.consultation.domain.usecase.SummarizeConsultationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConsultationSummaryUiState(
    val isLoading: Boolean = false,
    val summary: ConsultationSummary? = null,
    val selectedCategory: ActionItemCategory? = null,
    val isCalendarScheduled: Boolean = false,
    val toastMessage: String? = null
) {
    val filteredActionItems: List<ConsultationActionItem>
        get() = summary?.actionItems?.filter { item ->
            selectedCategory == null || item.category == selectedCategory
        } ?: emptyList()

    val totalItemCount: Int
        get() = summary?.actionItems?.size ?: 0

    val completedItemCount: Int
        get() = summary?.actionItems?.count { it.isCompleted } ?: 0

    val progressFraction: Float
        get() = if (totalItemCount > 0) completedItemCount.toFloat() / totalItemCount else 0f

    val progressFormattedBn: String
        get() = "${completedItemCount.toString().toBengaliDigits()} / ${totalItemCount.toString().toBengaliDigits()} সম্পন্ন"
}

sealed interface ConsultationSummaryUiEvent {
    data class OnToggleActionItem(val itemId: String) : ConsultationSummaryUiEvent
    data class OnSelectCategory(val category: ActionItemCategory?) : ConsultationSummaryUiEvent
    object OnScheduleCalendar : ConsultationSummaryUiEvent
    object OnDismissToast : ConsultationSummaryUiEvent
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
    private val summarizeConsultationUseCase: SummarizeConsultationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsultationSummaryUiState(isLoading = true))
    val uiState: StateFlow<ConsultationSummaryUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ConsultationSummarySideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadSummary()
    }

    fun onEvent(event: ConsultationSummaryUiEvent) {
        when (event) {
            is ConsultationSummaryUiEvent.OnToggleActionItem -> toggleActionItem(event.itemId)
            is ConsultationSummaryUiEvent.OnSelectCategory -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
            }
            ConsultationSummaryUiEvent.OnScheduleCalendar -> scheduleCalendar()
            ConsultationSummaryUiEvent.OnDismissToast -> {
                _uiState.update { it.copy(toastMessage = null) }
            }
        }
    }

    private fun loadSummary() {
        val summary = summarizeConsultationUseCase()
        _uiState.update {
            it.copy(
                isLoading = false,
                summary = summary
            )
        }
    }

    private fun toggleActionItem(itemId: String) {
        val currentSummary = _uiState.value.summary ?: return
        val updatedItems = currentSummary.actionItems.map { item ->
            if (item.id == itemId) item.copy(isCompleted = !item.isCompleted) else item
        }
        _uiState.update {
            it.copy(summary = currentSummary.copy(actionItems = updatedItems))
        }
    }

    private fun scheduleCalendar() {
        val currentSummary = _uiState.value.summary ?: return
        val followUpMillis = System.currentTimeMillis() + (currentSummary.followUpDays * 24L * 60L * 60L * 1000L)
        val title = "ডাক্তার ফলো-আপ ভিজিট: ${currentSummary.doctorName}"
        val description = "ফলো-আপ কারণ: ${currentSummary.followUpReasonBn}\n\nকরণীয়:\n" +
                currentSummary.actionItems.joinToString("\n") { "• ${it.task}" }

        _uiState.update {
            it.copy(
                isCalendarScheduled = true,
                toastMessage = "ক্যালেন্ডারে ফলো-আপ ভিজিট যুক্ত করা হচ্ছে..."
            )
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
                    "✅ ${currentSummary.followUpDays.toString().toBengaliDigits()} দিন পরের ফলো-আপ রিমাইন্ডার সেট করা হয়েছে।"
                )
            )
        }
    }
}
