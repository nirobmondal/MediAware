package com.example.mediaware.features.settings.presentation

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState

data class SettingsUiState(
    val cacheSizeMB: Double = 2.5,
    val medicineCacheCount: Int = 12,
    val testCacheCount: Int = 8,
    val isBiometricEnabled: Boolean = false,
    val showClearCacheDialog: Boolean = false,
    val isClearingCache: Boolean = false
) : ViewState

sealed interface SettingsUiEvent : ViewEvent {
    data class OnToggleBiometric(val enabled: Boolean) : SettingsUiEvent
    data object OnClearCacheClicked : SettingsUiEvent
    data object OnDismissClearCacheDialog : SettingsUiEvent
    data object OnConfirmClearCache : SettingsUiEvent
}

sealed interface SettingsSideEffect : ViewSideEffect {
    data class ShowToast(val messageBn: String) : SettingsSideEffect
    data object NavigateBack : SettingsSideEffect
}
