package com.example.mediaware.features.auth.presentation.login

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState

data class LoginUiState(
    val userName: String = "",
    val enteredPin: String = "",
    val failedAttempts: Int = 0,
    val lockRemainingSeconds: Int = 0,
    val isBiometricAvailable: Boolean = false,
    val errorMessageBn: String? = null
) : ViewState

sealed interface LoginUiEvent : ViewEvent {
    data class OnKeypadClick(val digit: Char) : LoginUiEvent
    data object OnBackspaceClick : LoginUiEvent
    data object OnBiometricAuthSuccess : LoginUiEvent
    data class OnBiometricAuthError(val errorBn: String) : LoginUiEvent
}

sealed interface LoginSideEffect : ViewSideEffect {
    data object NavigateToHome : LoginSideEffect
    data object TriggerHapticFeedback : LoginSideEffect
}
