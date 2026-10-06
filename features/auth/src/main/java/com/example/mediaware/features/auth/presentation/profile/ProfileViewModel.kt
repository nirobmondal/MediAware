package com.example.mediaware.features.auth.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.settings.AppSettingsManager
import com.example.mediaware.core.database.dao.UserProfileDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userId: String = "",
    val phoneNumber: String = "",
    val fullName: String = "",
    val age: String = "",
    val gender: String = "MALE",
    val bloodGroup: String = "A+",
    val selectedConditions: Set<String> = emptySet(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccessMessage: String? = null,
    val selectedAvatarId: String = "avatar_teal",
    val customPhotoUri: String? = null
)

sealed interface ProfileUiEvent {
    data class OnNameChanged(val name: String) : ProfileUiEvent
    data class OnAgeChanged(val age: String) : ProfileUiEvent
    data class OnGenderChanged(val gender: String) : ProfileUiEvent
    data class OnBloodGroupChanged(val bloodGroup: String) : ProfileUiEvent
    data class OnToggleCondition(val condition: String) : ProfileUiEvent
    data class OnSelectAvatar(val avatarId: String) : ProfileUiEvent
    data class OnUpdatePhotoUri(val uriString: String) : ProfileUiEvent
    data object OnToggleEditMode : ProfileUiEvent
    data object OnSaveProfile : ProfileUiEvent
    data object OnClearMessage : ProfileUiEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val appSettingsManager: AppSettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeAvatarSettings()
    }

    private fun observeAvatarSettings() {
        viewModelScope.launch {
            appSettingsManager.selectedAvatarId.collect { avatarId ->
                _uiState.update { it.copy(selectedAvatarId = avatarId) }
            }
        }
        viewModelScope.launch {
            appSettingsManager.customPhotoUri.collect { photoUri ->
                _uiState.update { it.copy(customPhotoUri = photoUri) }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userProfileDao.getUserProfileFlow().collect { profile ->
                if (profile != null) {
                    _uiState.update {
                        it.copy(
                            userId = profile.userId,
                            phoneNumber = profile.phoneNumber,
                            fullName = profile.fullName,
                            age = if (profile.age > 0) profile.age.toString() else "",
                            gender = profile.gender,
                            bloodGroup = profile.bloodGroup ?: "A+",
                            selectedConditions = profile.chronicConditions.toSet()
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.OnNameChanged -> _uiState.update { it.copy(fullName = event.name) }
            is ProfileUiEvent.OnAgeChanged -> _uiState.update { it.copy(age = event.age.filter { c -> c.isDigit() }) }
            is ProfileUiEvent.OnGenderChanged -> _uiState.update { it.copy(gender = event.gender) }
            is ProfileUiEvent.OnBloodGroupChanged -> _uiState.update { it.copy(bloodGroup = event.bloodGroup) }
            is ProfileUiEvent.OnToggleCondition -> {
                _uiState.update { state ->
                    val current = state.selectedConditions.toMutableSet()
                    if (current.contains(event.condition)) current.remove(event.condition) else current.add(event.condition)
                    state.copy(selectedConditions = current)
                }
            }
            is ProfileUiEvent.OnSelectAvatar -> {
                appSettingsManager.setAvatarId(event.avatarId)
                appSettingsManager.setCustomPhotoUri(null)
                _uiState.update { it.copy(selectedAvatarId = event.avatarId, customPhotoUri = null) }
            }
            is ProfileUiEvent.OnUpdatePhotoUri -> {
                appSettingsManager.setCustomPhotoUri(event.uriString)
                _uiState.update { it.copy(customPhotoUri = event.uriString) }
            }
            ProfileUiEvent.OnToggleEditMode -> _uiState.update { it.copy(isEditing = !it.isEditing) }
            ProfileUiEvent.OnSaveProfile -> saveProfile()
            ProfileUiEvent.OnClearMessage -> _uiState.update { it.copy(saveSuccessMessage = null) }
        }
    }

    private fun saveProfile() {
        val state = _uiState.value
        val ageInt = state.age.toIntOrNull() ?: 30
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            userProfileDao.updateProfileDetails(
                userId = state.userId,
                fullName = state.fullName.trim(),
                age = ageInt,
                gender = state.gender,
                bloodGroup = state.bloodGroup,
                chronicConditions = state.selectedConditions.toList()
            )
            _uiState.update {
                it.copy(
                    isSaving = false,
                    isEditing = false,
                    saveSuccessMessage = "প্রোফাইল সফলভাবে আপডেট করা হয়েছে"
                )
            }
        }
    }
}
