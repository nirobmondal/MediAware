package com.example.mediaware.features.auth.presentation.registration

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.core.designsystem.util.toEnglishDigits
import com.example.mediaware.features.auth.domain.usecase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : BaseViewModel<RegistrationUiState, RegistrationUiEvent, RegistrationSideEffect>(RegistrationUiState()) {

    override fun onEvent(event: RegistrationUiEvent) {
        when (event) {
            is RegistrationUiEvent.OnPhoneChanged -> {
                val cleaned = event.phone.toEnglishDigits().filter { it.isDigit() }
                if (cleaned.length <= 11) {
                    setState { copy(phoneNumber = cleaned, phoneErrorBn = null) }
                }
            }
            is RegistrationUiEvent.OnPinChanged -> {
                val cleaned = event.pin.toEnglishDigits().filter { it.isDigit() }
                if (cleaned.length <= 5) {
                    setState { copy(pin = cleaned, pinErrorBn = null) }
                }
            }
            is RegistrationUiEvent.OnConfirmPinChanged -> {
                val cleaned = event.confirmPin.toEnglishDigits().filter { it.isDigit() }
                if (cleaned.length <= 5) {
                    setState { copy(confirmPin = cleaned, pinErrorBn = null) }
                }
            }
            RegistrationUiEvent.SubmitRegistration -> submit()
        }
    }

    private fun submit() {
        val state = uiState.value
        if (state.phoneNumber.length != 11 || !state.phoneNumber.startsWith("01")) {
            setState { copy(phoneErrorBn = "সঠিক ১১ ডিজিটের বাংলাদেশী মোবাইল নম্বর দিন") }
            return
        }
        if (state.pin.length != 5) {
            setState { copy(pinErrorBn = "৫ সংখ্যার পিন দিন") }
            return
        }
        if (state.pin != state.confirmPin) {
            setState { copy(pinErrorBn = "দুই পিন মেলেনি, আবার লিখুন") }
            return
        }

        viewModelScope.launch {
            setState { copy(isSubmitting = true) }
            val result = registerUserUseCase(
                phone = state.phoneNumber,
                pin = state.pin,
                confirmPin = state.confirmPin
            )
            setState { copy(isSubmitting = false) }

            when (result) {
                is Resource.Success -> {
                    sendEffect(RegistrationSideEffect.NavigateToProfileSetup)
                }
                is Resource.Error -> {
                    setState { copy(pinErrorBn = result.messageBn) }
                    sendEffect(RegistrationSideEffect.ShowToast(result.messageBn))
                }
                Resource.Loading -> Unit
            }
        }
    }
}
