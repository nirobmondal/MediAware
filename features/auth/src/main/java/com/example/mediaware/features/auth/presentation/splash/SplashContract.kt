package com.example.mediaware.features.auth.presentation.splash

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState

data class SplashUiState(
    val isCheckingSession: Boolean = true
) : ViewState

sealed interface SplashUiEvent : ViewEvent {
    data object CheckSession : SplashUiEvent
}

sealed interface SplashSideEffect : ViewSideEffect {
    data object NavigateToRegister : SplashSideEffect
    data object NavigateToProfileSetup : SplashSideEffect
    data object NavigateToLogin : SplashSideEffect
}
