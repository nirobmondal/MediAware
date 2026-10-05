package com.example.mediaware.features.symptom.presentation.prep

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.features.symptom.domain.model.VisitPrepCard

data class VisitPrepUiState(
    val visitCard: VisitPrepCard? = null,
    val aiSymptomAnalysisBn: String? = null,
    val aiCheatQuestions: List<String> = emptyList(),
    val whatToShowDoctor: List<String> = emptyList(),
    val homeCareAdviceBn: String? = null,
    val needsDoctorVisit: Boolean = true,
    val isSavedToHealthMemory: Boolean = false,
    val isAiAnalyzing: Boolean = false,
    val isLoading: Boolean = true,
    val isPlayingTts: Boolean = false
) : ViewState

sealed interface VisitPrepUiEvent : ViewEvent {
    data class GenerateCard(val symptomIds: List<String>, val severity: Int, val durationBn: String) : VisitPrepUiEvent
    data object OnToggleTts : VisitPrepUiEvent
    data object OnNavigateToTestPrep : VisitPrepUiEvent
    data object OnFinishAndGoHome : VisitPrepUiEvent
}

sealed interface VisitPrepSideEffect : ViewSideEffect {
    data object NavigateToTestPrep : VisitPrepSideEffect
    data object NavigateToHome : VisitPrepSideEffect
    data class ShowToast(val messageBn: String) : VisitPrepSideEffect
}
