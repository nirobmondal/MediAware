package com.example.mediaware.features.auth.presentation.registration

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState

data class RegistrationUiState(
    val phoneNumber: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val phoneErrorBn: String? = null,
    val pinErrorBn: String? = null,
    val isSubmitting: Boolean = false
) : ViewState

sealed interface RegistrationUiEvent : ViewEvent {
    data class OnPhoneChanged(val phone: String) : RegistrationUiEvent
    data class OnPinChanged(val pin: String) : RegistrationUiEvent
    data class OnConfirmPinChanged(val confirmPin: String) : RegistrationUiEvent
    data object SubmitRegistration : RegistrationUiEvent
}

sealed interface RegistrationSideEffect : ViewSideEffect {
    data object NavigateToProfileSetup : RegistrationSideEffect
    data class ShowToast(val messageBn: String) : RegistrationSideEffect
}
