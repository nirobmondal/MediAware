package com.example.mediaware.features.history.presentation.timeline

import androidx.lifecycle.ViewModel
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.history.domain.engine.LongitudinalNarrativeEngine
import com.example.mediaware.features.history.domain.model.NarrativeTrend
import com.example.mediaware.features.history.domain.model.VitalRecord
import com.example.mediaware.features.history.domain.model.VitalType
import com.example.mediaware.features.history.domain.usecase.GetVitalsTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class HealthTimelineUiState(
    val selectedVitalType: VitalType = VitalType.BLOOD_SUGAR,
    val records: List<VitalRecord> = emptyList(),
    val trend: NarrativeTrend? = null,
    val isLoading: Boolean = false,
    val selectedPointIndex: Int? = null,
    val showAddDialog: Boolean = false
)

sealed interface HealthTimelineUiEvent {
    data class OnSelectVitalType(val vitalType: VitalType) : HealthTimelineUiEvent
    data class OnSelectDataPoint(val index: Int?) : HealthTimelineUiEvent
    object OnOpenAddDialog : HealthTimelineUiEvent
    object OnDismissAddDialog : HealthTimelineUiEvent
    data class OnAddReading(val value: Double, val notes: String) : HealthTimelineUiEvent
}

@HiltViewModel
class HealthTimelineViewModel @Inject constructor(
    private val getVitalsTimelineUseCase: GetVitalsTimelineUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthTimelineUiState(isLoading = true))
    val uiState: StateFlow<HealthTimelineUiState> = _uiState.asStateFlow()

    init {
        loadDataForType(VitalType.BLOOD_SUGAR)
    }

    fun onEvent(event: HealthTimelineUiEvent) {
        when (event) {
            is HealthTimelineUiEvent.OnSelectVitalType -> {
                loadDataForType(event.vitalType)
            }
            is HealthTimelineUiEvent.OnSelectDataPoint -> {
                _uiState.update { it.copy(selectedPointIndex = event.index) }
            }
            HealthTimelineUiEvent.OnOpenAddDialog -> {
                _uiState.update { it.copy(showAddDialog = true) }
            }
            HealthTimelineUiEvent.OnDismissAddDialog -> {
                _uiState.update { it.copy(showAddDialog = false) }
            }
            is HealthTimelineUiEvent.OnAddReading -> {
                addReading(event.value, event.notes)
            }
        }
    }

    private fun loadDataForType(vitalType: VitalType) {
        val result = getVitalsTimelineUseCase(vitalType)
        _uiState.update {
            it.copy(
                selectedVitalType = vitalType,
                records = result.records,
                trend = result.trend,
                selectedPointIndex = result.records.lastIndex.takeIf { idx -> idx >= 0 },
                isLoading = false
            )
        }
    }

    private fun addReading(value: Double, notes: String) {
        val currentRecords = _uiState.value.records.toMutableList()
        val currentType = _uiState.value.selectedVitalType
        val newRecord = VitalRecord(
            id = "rec_${System.currentTimeMillis()}",
            vitalType = currentType,
            value = value,
            timestamp = System.currentTimeMillis(),
            dateFormattedBn = "আজকের পরিমাপ",
            notesBn = notes.ifBlank { "স্বহস্তে যুক্ত পরিমাপ" }
        )
        currentRecords.add(newRecord)
        val trend = LongitudinalNarrativeEngine.computeTrend(currentRecords)
        _uiState.update {
            it.copy(
                records = currentRecords,
                trend = trend,
                selectedPointIndex = currentRecords.lastIndex,
                showAddDialog = false
            )
        }
    }
}
