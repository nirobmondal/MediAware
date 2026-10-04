package com.example.mediaware.features.auth.presentation.splash

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.features.auth.domain.usecase.CheckSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkSessionUseCase: CheckSessionUseCase
) : BaseViewModel<SplashUiState, SplashUiEvent, SplashSideEffect>(SplashUiState()) {

    init {
        onEvent(SplashUiEvent.CheckSession)
    }

    override fun onEvent(event: SplashUiEvent) {
        when (event) {
            SplashUiEvent.CheckSession -> checkUserSession()
        }
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            val session = checkSessionUseCase()
            // Guarantee minimum 400ms for smooth splash presentation
            delay(400)
            when {
                !session.isRegistered -> sendEffect(SplashSideEffect.NavigateToRegister)
                !session.isProfileComplete -> sendEffect(SplashSideEffect.NavigateToProfileSetup)
                else -> sendEffect(SplashSideEffect.NavigateToLogin)
            }
        }
    }
}
