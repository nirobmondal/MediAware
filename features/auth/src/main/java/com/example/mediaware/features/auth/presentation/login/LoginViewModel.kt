package com.example.mediaware.features.auth.presentation.login

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.common.security.PinSecurityManager
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel<LoginUiState, LoginUiEvent, LoginSideEffect>(LoginUiState()) {

    private var lockoutJob: Job? = null

    init {
        loadUserGreeting()
    }

    private fun loadUserGreeting() {
        viewModelScope.launch {
            val credentials = userRepository.getUserCredentials()
            if (credentials != null) {
                userRepository.getUserProfileFlow().collect { profile ->
                    if (profile != null && profile.fullName.isNotBlank()) {
                        setState {
                            copy(
                                userName = profile.fullName,
                                isBiometricAvailable = profile.isBiometricEnabled
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.OnKeypadClick -> handleKeypadInput(event.digit)
            LoginUiEvent.OnBackspaceClick -> handleBackspace()
            LoginUiEvent.OnBiometricAuthSuccess -> sendEffect(LoginSideEffect.NavigateToHome)
            is LoginUiEvent.OnBiometricAuthError -> setState { copy(errorMessageBn = event.errorBn) }
        }
    }

    private fun handleKeypadInput(digit: Char) {
        if (uiState.value.lockRemainingSeconds > 0) return
        val currentPin = uiState.value.enteredPin
        if (currentPin.length < 4) {
            val updatedPin = currentPin + digit
            setState { copy(enteredPin = updatedPin, errorMessageBn = null) }
            if (updatedPin.length == 4) {
                verifyPin(updatedPin)
            }
        }
    }

    private fun handleBackspace() {
        if (uiState.value.enteredPin.isNotEmpty()) {
            setState { copy(enteredPin = enteredPin.dropLast(1)) }
        }
    }

    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            val credentials = userRepository.getUserCredentials()
            if (credentials == null) {
                setState { copy(errorMessageBn = "কোনো সংরক্ষিত তথ্য পাওয়া যায়নি", enteredPin = "") }
                return@launch
            }

            val isValid = PinSecurityManager.verifyPin(
                enteredPin = pin,
                storedHash = credentials.pinHash,
                storedSalt = credentials.pinSalt
            )

            if (isValid) {
                setState { copy(failedAttempts = 0, enteredPin = "") }
                sendEffect(LoginSideEffect.NavigateToHome)
            } else {
                val attempts = uiState.value.failedAttempts + 1
                sendEffect(LoginSideEffect.TriggerHapticFeedback)
                if (attempts >= 5) {
                    startLockout(60)
                } else {
                    val remaining = 5 - attempts
                    setState {
                        copy(
                            enteredPin = "",
                            failedAttempts = attempts,
                            errorMessageBn = "ভুল পিন। আর ${remaining.toBengaliDigits()} বার চেষ্টা করতে পারবেন।"
                        )
                    }
                }
            }
        }
    }

    private fun startLockout(seconds: Int) {
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                setState {
                    copy(
                        lockRemainingSeconds = i,
                        enteredPin = "",
                        errorMessageBn = "অতিরিক্ত ভুল পিন। ${i.toBengaliDigits()} সেকেন্ড পর আবার চেষ্টা করুন।"
                    )
                }
                delay(1000)
            }
            setState { copy(lockRemainingSeconds = 0, failedAttempts = 0, errorMessageBn = null) }
        }
    }
}
