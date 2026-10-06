package com.example.mediaware.features.symptom.presentation.select

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.features.symptom.domain.model.Symptom

data class SymptomSelectUiState(
    val searchQuery: String = "",
    val availableSymptoms: List<Symptom> = emptyList(),
    val filteredSymptoms: List<Symptom> = emptyList(),
    val selectedSymptomIds: Set<String> = emptySet(),
    val isListeningVoice: Boolean = false,
    val voiceTranscriptBn: String? = null
) : ViewState

sealed interface SymptomSelectUiEvent : ViewEvent {
    data class OnSearchQueryChanged(val query: String) : SymptomSelectUiEvent
    data class OnSymptomToggled(val symptomId: String) : SymptomSelectUiEvent
    data class OnRemoveSelectedSymptom(val symptomId: String) : SymptomSelectUiEvent
    data object OnToggleVoiceInput : SymptomSelectUiEvent
    data object OnStartListeningVoice : SymptomSelectUiEvent
    data object OnStopListeningVoice : SymptomSelectUiEvent
    data class OnVoiceTranscriptReceived(val text: String) : SymptomSelectUiEvent
    data object OnProceedToFollowup : SymptomSelectUiEvent
}

sealed interface SymptomSelectSideEffect : ViewSideEffect {
    data class NavigateToFollowup(val symptomIds: List<String>) : SymptomSelectSideEffect
    data class ShowToast(val messageBn: String) : SymptomSelectSideEffect
    data object NavigateBack : SymptomSelectSideEffect
}
