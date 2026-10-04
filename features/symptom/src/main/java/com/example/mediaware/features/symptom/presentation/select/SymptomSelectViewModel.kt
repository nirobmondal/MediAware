package com.example.mediaware.features.symptom.presentation.select

import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.features.symptom.domain.repository.SymptomCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SymptomSelectViewModel @Inject constructor() :
    BaseViewModel<SymptomSelectUiState, SymptomSelectUiEvent, SymptomSelectSideEffect>(
        SymptomSelectUiState(
            availableSymptoms = SymptomCatalog.ALL_SYMPTOMS,
            filteredSymptoms = SymptomCatalog.ALL_SYMPTOMS
        )
    ) {

    override fun onEvent(event: SymptomSelectUiEvent) {
        when (event) {
            is SymptomSelectUiEvent.OnSearchQueryChanged -> filterSymptoms(event.query)
            is SymptomSelectUiEvent.OnSymptomToggled -> toggleSymptom(event.symptomId)
            is SymptomSelectUiEvent.OnRemoveSelectedSymptom -> removeSymptom(event.symptomId)
            SymptomSelectUiEvent.OnToggleVoiceInput -> {
                val nextState = !uiState.value.isListeningVoice
                setState { copy(isListeningVoice = nextState) }
                if (nextState) {
                    sendEffect(SymptomSelectSideEffect.ShowToast("আপনার শারীরিক সমস্যার কথা বলুন..."))
                }
            }
            is SymptomSelectUiEvent.OnVoiceTranscriptReceived -> {
                handleVoiceTranscript(event.text)
            }
            SymptomSelectUiEvent.OnProceedToFollowup -> {
                val selected = uiState.value.selectedSymptomIds.toList()
                if (selected.isEmpty()) {
                    sendEffect(SymptomSelectSideEffect.ShowToast("কমপক্ষে একটি লক্ষণ বাছাই করুন"))
                } else {
                    sendEffect(SymptomSelectSideEffect.NavigateToFollowup(selected))
                }
            }
        }
    }

    private fun filterSymptoms(query: String) {
        val trimmed = query.trim().lowercase()
        val all = uiState.value.availableSymptoms
        val filtered = if (trimmed.isEmpty()) {
            all
        } else {
            all.filter {
                it.nameBn.lowercase().contains(trimmed) ||
                it.anatomicalRegionBn.lowercase().contains(trimmed) ||
                it.id.lowercase().contains(trimmed)
            }
        }
        setState { copy(searchQuery = query, filteredSymptoms = filtered) }
    }

    private fun toggleSymptom(id: String) {
        val current = uiState.value.selectedSymptomIds
        val updated = if (current.contains(id)) {
            current - id
        } else {
            current + id
        }
        setState { copy(selectedSymptomIds = updated) }
    }

    private fun removeSymptom(id: String) {
        val current = uiState.value.selectedSymptomIds
        setState { copy(selectedSymptomIds = current - id) }
    }

    private fun handleVoiceTranscript(transcript: String) {
        val lower = transcript.lowercase()
        val matchedIds = mutableSetOf<String>()
        for (symptom in uiState.value.availableSymptoms) {
            if (lower.contains(symptom.nameBn.lowercase()) ||
                lower.contains(symptom.anatomicalRegionBn.lowercase())) {
                matchedIds.add(symptom.id)
            }
        }
        val current = uiState.value.selectedSymptomIds
        setState {
            copy(
                isListeningVoice = false,
                voiceTranscriptBn = transcript,
                selectedSymptomIds = current + matchedIds
            )
        }
        if (matchedIds.isNotEmpty()) {
            sendEffect(SymptomSelectSideEffect.ShowToast("${matchedIds.size}টি লক্ষণ শনাক্ত করা হয়েছে"))
        } else {
            sendEffect(SymptomSelectSideEffect.ShowToast("বলা বক্তব্য থেকে তালিকাভুক্ত কোনো লক্ষণ মেলেনি"))
        }
    }
}
