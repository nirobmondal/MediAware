package com.example.mediaware.features.symptom.presentation.select

import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.features.symptom.domain.model.Symptom
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
            is SymptomSelectUiEvent.OnAddCustomSymptom -> addCustomSymptom(event.symptomName)
            SymptomSelectUiEvent.OnToggleVoiceInput -> {
                val nextState = !uiState.value.isListeningVoice
                setState { copy(isListeningVoice = nextState) }
            }
            SymptomSelectUiEvent.OnStartListeningVoice -> {
                setState { copy(isListeningVoice = true) }
            }
            SymptomSelectUiEvent.OnStopListeningVoice -> {
                setState { copy(isListeningVoice = false) }
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
        val lower = transcript.lowercase().trim()
        val matchedIds = mutableSetOf<String>()

        // 1. Direct contains match
        for (symptom in uiState.value.availableSymptoms) {
            if (lower.contains(symptom.nameBn.lowercase()) ||
                lower.contains(symptom.anatomicalRegionBn.lowercase())) {
                matchedIds.add(symptom.id)
            }
        }

        // 2. Intelligent conversational Bengali keyword matching
        val keywordMap = mapOf(
            "মাথা" to listOf("headache"),
            "তীব্র মাথা" to listOf("severe_headache"),
            "চোখ" to listOf("blurred_vision"),
            "ঝাপসা" to listOf("blurred_vision"),
            "কথা" to listOf("slurred_speech"),
            "জড়িয়ে" to listOf("slurred_speech"),
            "বুক" to listOf("chest_pain"),
            "বুকে ব্যথা" to listOf("chest_pain"),
            "ধড়ফড়" to listOf("palpitations"),
            "শ্বাস" to listOf("breathlessness"),
            "দম" to listOf("breathlessness"),
            "কাশি" to listOf("cough"),
            "কফ" to listOf("cough"),
            "বমি" to listOf("vomiting"),
            "পেট" to listOf("abdominal_pain"),
            "জ্বর" to listOf("high_fever"),
            "অচেতন" to listOf("unconsciousness"),
            "জ্ঞান" to listOf("unconsciousness"),
            "অবশ" to listOf("one_sided_weakness"),
            "প্যারালাইসিস" to listOf("one_sided_weakness"),
            "দুর্বল" to listOf("extreme_fatigue"),
            "ক্লান্ত" to listOf("extreme_fatigue"),
            "পা ফোলা" to listOf("leg_swelling"),
            "ফোলা" to listOf("leg_swelling"),
            "রক্ত" to listOf("anemia"),
            "ফ্যাকাশে" to listOf("anemia"),
            "কোমর" to listOf("back_pain"),
            "পিঠ" to listOf("back_pain"),
            "প্রস্রাব" to listOf("urinary_burning"),
            "জ্বালাপোড়া" to listOf("urinary_burning")
        )

        for ((keyword, ids) in keywordMap) {
            if (lower.contains(keyword)) {
                matchedIds.addAll(ids)
            }
        }

        val current = uiState.value.selectedSymptomIds
        val updated = current + matchedIds
        
        filterSymptoms(transcript)

        setState {
            copy(
                isListeningVoice = false,
                voiceTranscriptBn = transcript,
                selectedSymptomIds = updated
            )
        }

        if (matchedIds.isNotEmpty()) {
            sendEffect(SymptomSelectSideEffect.ShowToast("${matchedIds.size}টি লক্ষণ চিহ্নিত হয়েছে: \"$transcript\""))
        } else if (transcript.isNotBlank()) {
            addCustomSymptom(transcript.trim())
        }
    }

    private fun addCustomSymptom(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return

        val existing = uiState.value.availableSymptoms.find {
            it.nameBn.equals(trimmed, ignoreCase = true)
        }

        if (existing != null) {
            val updated = uiState.value.selectedSymptomIds + existing.id
            setState {
                copy(
                    selectedSymptomIds = updated,
                    searchQuery = "",
                    filteredSymptoms = availableSymptoms
                )
            }
            sendEffect(SymptomSelectSideEffect.ShowToast("\"${existing.nameBn}\" নির্বাচিত হয়েছে"))
            return
        }

        val customId = "custom_${trimmed}"
        val newSymptom = Symptom(
            id = customId,
            nameBn = trimmed,
            anatomicalRegionBn = "কাস্টম লক্ষণ",
            isRedFlagPotential = false,
            iconName = "healing"
        )

        val updatedAvailable = uiState.value.availableSymptoms + newSymptom
        val updatedSelected = uiState.value.selectedSymptomIds + customId

        setState {
            copy(
                availableSymptoms = updatedAvailable,
                filteredSymptoms = updatedAvailable,
                selectedSymptomIds = updatedSelected,
                searchQuery = ""
            )
        }
        sendEffect(SymptomSelectSideEffect.ShowToast("\"$trimmed\" লক্ষণটি যোগ করা হয়েছে"))
    }
}
