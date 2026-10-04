package com.example.mediaware.features.auth.presentation.setup

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.core.designsystem.util.toEnglishDigits
import com.example.mediaware.features.auth.domain.usecase.SaveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val saveProfileUseCase: SaveProfileUseCase
) : BaseViewModel<ProfileSetupUiState, ProfileSetupUiEvent, ProfileSetupSideEffect>(ProfileSetupUiState()) {

    override fun onEvent(event: ProfileSetupUiEvent) {
        when (event) {
            is ProfileSetupUiEvent.OnNameChanged -> {
                setState { copy(fullName = event.name, validationErrorBn = null) }
            }
            is ProfileSetupUiEvent.OnAgeChanged -> {
                val digits = event.age.toEnglishDigits().filter { it.isDigit() }
                if (digits.length <= 3) {
                    setState { copy(ageString = digits, validationErrorBn = null) }
                }
            }
            is ProfileSetupUiEvent.OnGenderSelected -> {
                setState { copy(selectedGender = event.gender) }
            }
            is ProfileSetupUiEvent.OnBloodGroupSelected -> {
                setState {
                    copy(
                        selectedBloodGroup = if (selectedBloodGroup == event.bloodGroup) null else event.bloodGroup
                    )
                }
            }
            is ProfileSetupUiEvent.OnToggleChronicCondition -> {
                val current = uiState.value.selectedChronicConditions.toMutableSet()
                if (event.conditionKey == "নেই") {
                    current.clear()
                    current.add("নেই")
                } else {
                    current.remove("নেই")
                    if (current.contains(event.conditionKey)) {
                        current.remove(event.conditionKey)
                    } else {
                        current.add(event.conditionKey)
                    }
                }
                setState { copy(selectedChronicConditions = current) }
            }
            ProfileSetupUiEvent.SaveProfile -> saveProfile()
        }
    }

    private fun saveProfile() {
        val state = uiState.value
        viewModelScope.launch {
            setState { copy(isSaving = true) }
            val result = saveProfileUseCase(
                name = state.fullName,
                ageString = state.ageString,
                gender = state.selectedGender,
                bloodGroup = state.selectedBloodGroup,
                conditions = state.selectedChronicConditions
            )
            setState { copy(isSaving = false) }

            when (result) {
                is Resource.Success -> {
                    sendEffect(ProfileSetupSideEffect.NavigateToHome)
                }
                is Resource.Error -> {
                    setState { copy(validationErrorBn = result.messageBn) }
                }
                Resource.Loading -> Unit
            }
        }
    }
}
