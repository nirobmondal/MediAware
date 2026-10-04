package com.example.mediaware.features.symptom.presentation.followup

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.features.symptom.domain.model.Symptom

data class SymptomFollowupUiState(
    val selectedSymptomIds: List<String> = emptyList(),
    val selectedSymptoms: List<Symptom> = emptyList(),
    val severityRating: Int = 5,
    val selectedDurationBn: String = "২-৩ দিন",
    val availableDurationsBn: List<String> = listOf("আজ শুরু", "২-৩ দিন", "১-২ সপ্তাহ", "১ মাস+"),
    val isEvaluating: Boolean = false
) : ViewState

sealed interface SymptomFollowupUiEvent : ViewEvent {
    data class LoadSymptoms(val symptomIds: List<String>) : SymptomFollowupUiEvent
    data class OnSeverityChanged(val severity: Int) : SymptomFollowupUiEvent
    data class OnDurationSelected(val durationBn: String) : SymptomFollowupUiEvent
    data object OnEvaluateAndProceed : SymptomFollowupUiEvent
}

sealed interface SymptomFollowupSideEffect : ViewSideEffect {
    data class NavigateToEmergency(val reasonBn: String, val matchedSymptoms: List<String>) : SymptomFollowupSideEffect
    data class NavigateToVisitPrep(val symptomIds: List<String>, val severity: Int, val durationBn: String) : SymptomFollowupSideEffect
    data object NavigateBack : SymptomFollowupSideEffect
}
