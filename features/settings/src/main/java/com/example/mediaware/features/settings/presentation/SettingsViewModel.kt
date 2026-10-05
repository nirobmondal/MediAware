package com.example.mediaware.features.settings.presentation

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.core.domain.repository.UserRepository
import com.example.mediaware.features.settings.domain.usecase.ClearSmartCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val clearSmartCacheUseCase: ClearSmartCacheUseCase,
    private val userRepository: UserRepository
) : BaseViewModel<SettingsUiState, SettingsUiEvent, SettingsSideEffect>(SettingsUiState()) {

    init {
        viewModelScope.launch {
            val user = userRepository.getUserCredentials()
            setState { copy(isBiometricEnabled = user?.isBiometricEnabled ?: false) }

            userRepository.getUserProfileFlow().collect { profile ->
                if (profile != null) {
                    setState {
                        copy(
                            userName = profile.fullName,
                            userPhone = profile.phoneNumber,
                            bloodGroup = profile.bloodGroup ?: "A+",
                            userAge = profile.age,
                            isBiometricEnabled = profile.isBiometricEnabled
                        )
                    }
                }
            }
        }
    }

    override fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.OnToggleBiometric -> {
                viewModelScope.launch {
                    userRepository.setBiometricEnabled(event.enabled)
                    setState { copy(isBiometricEnabled = event.enabled) }
                }
            }
            SettingsUiEvent.OnClearCacheClicked -> setState { copy(showClearCacheDialog = true) }
            SettingsUiEvent.OnDismissClearCacheDialog -> setState { copy(showClearCacheDialog = false) }
            SettingsUiEvent.OnConfirmClearCache -> clearCache()
        }
    }

    private fun clearCache() {
        setState { copy(isClearingCache = true, showClearCacheDialog = false) }
        viewModelScope.launch {
            when (val result = clearSmartCacheUseCase()) {
                is Resource.Success -> {
                    setState { copy(cacheSizeMB = 0.0, medicineCacheCount = 0, testCacheCount = 0, isClearingCache = false) }
                    sendEffect(SettingsSideEffect.ShowToast("ক্যাশে সফলভাবে পরিষ্কার করা হয়েছে"))
                }
                is Resource.Error -> {
                    setState { copy(isClearingCache = false) }
                    sendEffect(SettingsSideEffect.ShowToast(result.messageBn))
                }
                else -> {}
            }
        }
    }
}
