package com.example.mediaware.features.home.presentation

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState

data class HomeUiState(
    val userName: String = "",
    val isOnline: Boolean = true,
    val upcomingReminder: UpcomingReminderUiModel? = null,
    val hasPendingLabReportToVerify: Boolean = false,
    val isLoading: Boolean = true
) : ViewState

data class UpcomingReminderUiModel(
    val reminderId: String,
    val titleBn: String,
    val timeFormattedBn: String,
    val instructionBn: String,
    val isFastingAlert: Boolean
)

sealed interface HomeUiEvent : ViewEvent {
    data object RefreshData : HomeUiEvent
    data class OnDismissReminder(val reminderId: String) : HomeUiEvent
}

sealed interface HomeSideEffect : ViewSideEffect {
    data class NavigateTo(val route: String) : HomeSideEffect
}
