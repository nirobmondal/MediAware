package com.example.mediaware.features.symptom.presentation.fasting

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.features.symptom.domain.model.FastingGuideline

data class TestPrepGuideUiState(
    val selectedTestId: String = "fbs",
    val availableGuidelines: List<FastingGuideline> = emptyList(),
    val isAlarmScheduled: Boolean = false,
    val scheduledTimeFormattedBn: String? = null,
    val scheduledHours: Int = 8
) : ViewState

sealed interface TestPrepGuideUiEvent : ViewEvent {
    data class OnSelectTest(val testId: String) : TestPrepGuideUiEvent
    data class OnScheduleFastingAlarm(val fastingHours: Int = 8) : TestPrepGuideUiEvent
    data object OnCancelAlarm : TestPrepGuideUiEvent
}

sealed interface TestPrepGuideSideEffect : ViewSideEffect {
    data class ShowToast(val messageBn: String) : TestPrepGuideSideEffect
    data object NavigateBack : TestPrepGuideSideEffect
}
