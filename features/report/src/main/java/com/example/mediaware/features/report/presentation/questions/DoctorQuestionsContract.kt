package com.example.mediaware.features.report.presentation.questions

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.features.report.domain.model.DoctorQuestion

data class DoctorQuestionsUiState(
    val questions: List<DoctorQuestion> = emptyList(),
    val isLoading: Boolean = true,
    val savedCount: Int = 0
) : ViewState

sealed interface DoctorQuestionsUiEvent : ViewEvent {
    data class LoadQuestions(val encodedItems: String) : DoctorQuestionsUiEvent
    data class OnToggleQuestionChecked(val questionId: String) : DoctorQuestionsUiEvent
    data object OnSaveToConsultationChecklist : DoctorQuestionsUiEvent
    data object OnNavigateHome : DoctorQuestionsUiEvent
}

sealed interface DoctorQuestionsSideEffect : ViewSideEffect {
    data object NavigateHome : DoctorQuestionsSideEffect
    data class ShowToast(val messageBn: String) : DoctorQuestionsSideEffect
}
