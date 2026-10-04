package com.example.mediaware.features.report.presentation.analysis

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.features.report.domain.model.LabReportAnalysis

data class ReportAnalysisUiState(
    val analysis: LabReportAnalysis? = null,
    val isPlayingTts: Boolean = false,
    val currentlyPlayingItemKey: String? = null,
    val isLoading: Boolean = true
) : ViewState

sealed interface ReportAnalysisUiEvent : ViewEvent {
    data class LoadAnalysis(val encodedItems: String) : ReportAnalysisUiEvent
    data class OnToggleItemTts(val itemKey: String) : ReportAnalysisUiEvent
    data object OnToggleFullTts : ReportAnalysisUiEvent
    data object OnProceedToQuestions : ReportAnalysisUiEvent
}

sealed interface ReportAnalysisSideEffect : ViewSideEffect {
    data class NavigateToQuestions(val encodedItems: String) : ReportAnalysisSideEffect
    data class ShowToast(val messageBn: String) : ReportAnalysisSideEffect
    data object NavigateBack : ReportAnalysisSideEffect
}
