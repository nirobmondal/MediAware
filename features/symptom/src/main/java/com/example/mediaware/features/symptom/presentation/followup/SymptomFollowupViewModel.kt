package com.example.mediaware.features.symptom.presentation.followup

import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.features.symptom.domain.model.Urgency
import com.example.mediaware.features.symptom.domain.repository.SymptomCatalog
import com.example.mediaware.features.symptom.domain.usecase.CheckEmergencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SymptomFollowupViewModel @Inject constructor(
    private val checkEmergencyUseCase: CheckEmergencyUseCase
) : BaseViewModel<SymptomFollowupUiState, SymptomFollowupUiEvent, SymptomFollowupSideEffect>(
    SymptomFollowupUiState()
) {

    override fun onEvent(event: SymptomFollowupUiEvent) {
        when (event) {
            is SymptomFollowupUiEvent.LoadSymptoms -> {
                val catalog = SymptomCatalog.ALL_SYMPTOMS.associateBy { it.id }
                val resolved = event.symptomIds.mapNotNull { catalog[it] }
                setState {
                    copy(
                        selectedSymptomIds = event.symptomIds,
                        selectedSymptoms = resolved
                    )
                }
            }
            is SymptomFollowupUiEvent.OnSeverityChanged -> {
                setState { copy(severityRating = event.severity) }
            }
            is SymptomFollowupUiEvent.OnDurationSelected -> {
                setState { copy(selectedDurationBn = event.durationBn) }
            }
            SymptomFollowupUiEvent.OnEvaluateAndProceed -> {
                evaluateTriage()
            }
        }
    }

    /**
     * 0ms Deterministic Offline Evaluation (Guardrail #4)
     */
    private fun evaluateTriage() {
        val state = uiState.value
        val symptomIdsSet = state.selectedSymptomIds.toSet()
        val symptomNamesSet = state.selectedSymptoms.map { it.nameBn }.toSet()

        // Synchronous main-thread set matching - 0ms, zero network calls
        val result = checkEmergencyUseCase(
            selectedSymptomIds = symptomIdsSet,
            selectedSymptomNames = symptomNamesSet,
            severity = state.severityRating
        )

        if (result.isEmergency && result.urgency == Urgency.IMMEDIATE_999) {
            sendEffect(
                SymptomFollowupSideEffect.NavigateToEmergency(
                    reasonBn = result.reasonBn,
                    matchedSymptoms = result.matchedSymptoms.toList()
                )
            )
        } else {
            sendEffect(
                SymptomFollowupSideEffect.NavigateToVisitPrep(
                    symptomIds = state.selectedSymptomIds,
                    severity = state.severityRating,
                    durationBn = state.selectedDurationBn
                )
            )
        }
    }
}
