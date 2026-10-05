package com.example.mediaware.features.symptom.presentation.prep

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.ai.GeminiAiClient
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.domain.repository.UserRepository
import com.example.mediaware.features.symptom.domain.repository.SymptomCatalog
import com.example.mediaware.features.symptom.domain.usecase.GenerateVisitPrepUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VisitPrepViewModel @Inject constructor(
    private val generateVisitPrepUseCase: GenerateVisitPrepUseCase,
    private val userRepository: UserRepository,
    private val geminiAiClient: GeminiAiClient
) : BaseViewModel<VisitPrepUiState, VisitPrepUiEvent, VisitPrepSideEffect>(VisitPrepUiState()) {

    override fun onEvent(event: VisitPrepUiEvent) {
        when (event) {
            is VisitPrepUiEvent.GenerateCard -> {
                generateCard(event.symptomIds, event.severity, event.durationBn)
            }
            VisitPrepUiEvent.OnToggleTts -> {
                val next = !uiState.value.isPlayingTts
                setState { copy(isPlayingTts = next) }
                if (next) {
                    sendEffect(VisitPrepSideEffect.ShowToast("অডিও চালু হয়েছে: ডাক্তারের কাছে বলার পয়েন্টসমূহ"))
                }
            }
            VisitPrepUiEvent.OnNavigateToTestPrep -> {
                sendEffect(VisitPrepSideEffect.NavigateToTestPrep)
            }
            VisitPrepUiEvent.OnFinishAndGoHome -> {
                sendEffect(VisitPrepSideEffect.NavigateToHome)
            }
        }
    }

    private fun generateCard(symptomIds: List<String>, severity: Int, durationBn: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, isAiAnalyzing = true) }
            val profile = userRepository.getUserProfileFlow().firstOrNull()
            val catalog = SymptomCatalog.ALL_SYMPTOMS.associateBy { it.id }
            val resolvedSymptoms = symptomIds.mapNotNull { catalog[it] }

            val initialCard = generateVisitPrepUseCase(
                userProfile = profile,
                symptoms = resolvedSymptoms,
                severityRating = severity,
                durationBn = durationBn
            )

            setState {
                copy(
                    visitCard = initialCard,
                    isLoading = false
                )
            }

            // Asynchronously generate AI Symptom Analysis & Doctor Cheat Questions
            val (aiAnalysis, aiQuestions) = geminiAiClient.analyzeDiseaseAndSymptoms(
                symptoms = resolvedSymptoms.map { it.nameBn },
                severity = severity,
                duration = durationBn,
                chronicConditions = profile?.chronicConditions ?: emptyList(),
                age = profile?.age,
                gender = profile?.gender?.displayNameBn
            )

            val updatedQuestions = if (aiQuestions.isNotEmpty()) aiQuestions else initialCard.doctorQuestionsBn
            val updatedCard = initialCard.copy(doctorQuestionsBn = updatedQuestions)

            setState {
                copy(
                    visitCard = updatedCard,
                    aiSymptomAnalysisBn = aiAnalysis,
                    aiCheatQuestions = updatedQuestions,
                    isAiAnalyzing = false
                )
            }
        }
    }
}
