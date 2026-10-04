package com.example.mediaware.features.history.presentation.adherence

import androidx.lifecycle.ViewModel
import com.example.mediaware.features.history.domain.model.AdherenceDay
import com.example.mediaware.features.history.domain.model.AdherenceRecord
import com.example.mediaware.features.history.domain.model.AdherenceStatus
import com.example.mediaware.features.history.domain.model.TitrationRecord
import com.example.mediaware.features.history.domain.usecase.GetAdherenceHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MedicineHistoryUiState(
    val adherencePercentage: Int = 0,
    val totalDosesScheduled: Int = 0,
    val dosesTaken: Int = 0,
    val dosesMissed: Int = 0,
    val dosesSnoozed: Int = 0,
    val calendarDays: List<AdherenceDay> = emptyList(),
    val todayRecords: List<AdherenceRecord> = emptyList(),
    val titrationHistory: List<TitrationRecord> = emptyList(),
    val selectedDay: Int = 4,
    val isLoading: Boolean = false
)

sealed interface MedicineHistoryUiEvent {
    data class OnSelectDay(val dayOfMonth: Int) : MedicineHistoryUiEvent
    data class OnUpdateRecordStatus(val recordId: String, val newStatus: AdherenceStatus) : MedicineHistoryUiEvent
}

@HiltViewModel
class MedicineHistoryViewModel @Inject constructor(
    private val getAdherenceHistoryUseCase: GetAdherenceHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicineHistoryUiState(isLoading = true))
    val uiState: StateFlow<MedicineHistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val result = getAdherenceHistoryUseCase()
        _uiState.update {
            it.copy(
                adherencePercentage = result.adherencePercentage,
                totalDosesScheduled = result.totalDosesScheduled,
                dosesTaken = result.dosesTaken,
                dosesMissed = result.dosesMissed,
                dosesSnoozed = result.dosesSnoozed,
                calendarDays = result.calendarDays,
                todayRecords = result.todayRecords,
                titrationHistory = result.titrationHistory,
                isLoading = false
            )
        }
    }

    fun onEvent(event: MedicineHistoryUiEvent) {
        when (event) {
            is MedicineHistoryUiEvent.OnSelectDay -> {
                _uiState.update { it.copy(selectedDay = event.dayOfMonth) }
            }
            is MedicineHistoryUiEvent.OnUpdateRecordStatus -> {
                updateRecordStatus(event.recordId, event.newStatus)
            }
        }
    }

    private fun updateRecordStatus(recordId: String, newStatus: AdherenceStatus) {
        val current = _uiState.value.todayRecords
        val updated = current.map { record ->
            if (record.id == recordId) record.copy(status = newStatus) else record
        }
        val takenCount = updated.count { it.status == AdherenceStatus.TAKEN }
        val missedCount = updated.count { it.status == AdherenceStatus.MISSED }
        val snoozedCount = updated.count { it.status == AdherenceStatus.SNOOZED }

        _uiState.update {
            it.copy(
                todayRecords = updated,
                dosesTaken = it.dosesTaken + if (newStatus == AdherenceStatus.TAKEN) 1 else -1,
                adherencePercentage = (((it.dosesTaken + 1).toFloat() / it.totalDosesScheduled) * 100).toInt().coerceIn(0, 100)
            )
        }
    }
}
