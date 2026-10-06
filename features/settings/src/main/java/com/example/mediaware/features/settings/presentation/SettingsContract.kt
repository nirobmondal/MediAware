package com.example.mediaware.features.settings.presentation

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState

data class SettingsUiState(
    val userName: String = "",
    val userPhone: String = "",
    val bloodGroup: String = "",
    val userAge: Int = 0,
    val cacheSizeMB: Double = 0.5,
    val medicineCacheCount: Int = 6,
    val testCacheCount: Int = 4,
    val isBiometricEnabled: Boolean = false,
    val isLargeTextEnabled: Boolean = false,
    val isAudioGuidanceEnabled: Boolean = true,
    val selectedAvatarId: String = "avatar_teal",
    val customPhotoUri: String? = null,
    val showClearCacheDialog: Boolean = false,
    val isClearingCache: Boolean = false
) : ViewState

sealed interface SettingsUiEvent : ViewEvent {
    data class OnToggleBiometric(val enabled: Boolean) : SettingsUiEvent
    data class OnToggleLargeText(val enabled: Boolean) : SettingsUiEvent
    data class OnToggleAudioGuidance(val enabled: Boolean) : SettingsUiEvent
    data object OnTestAudioGuidance : SettingsUiEvent
    data object OnClearCacheClicked : SettingsUiEvent
    data object OnDismissClearCacheDialog : SettingsUiEvent
    data object OnConfirmClearCache : SettingsUiEvent
}

sealed interface SettingsSideEffect : ViewSideEffect {
    data class ShowToast(val messageBn: String) : SettingsSideEffect
    data object NavigateBack : SettingsSideEffect
}
