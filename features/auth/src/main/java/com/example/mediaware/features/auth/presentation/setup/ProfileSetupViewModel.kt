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
                if (event.conditionKey == "কোনোটিই নয় / সুস্থ" || event.conditionKey == "নেই") {
                    current.clear()
                    current.add("নেই")
                } else {
                    current.remove("নেই")
                    current.remove("কোনোটিই নয় / সুস্থ")
                    if (current.contains(event.conditionKey)) {
                        current.remove(event.conditionKey)
                    } else {
                        current.add(event.conditionKey)
                    }
                }
                setState { copy(selectedChronicConditions = current) }
            }
            is ProfileSetupUiEvent.OnOtherConditionChanged -> {
                setState { copy(otherCondition = event.other) }
            }
            ProfileSetupUiEvent.OnNextStepClicked -> handleNextStep()
            ProfileSetupUiEvent.OnPreviousStepClicked -> handlePreviousStep()
            ProfileSetupUiEvent.SaveProfile -> saveProfile()
        }
    }

    private fun handleNextStep() {
        val state = uiState.value
        when (state.currentStep) {
            1 -> {
                val cleanName = state.fullName.trim()
                if (cleanName.length < 2) {
                    setState { copy(validationErrorBn = "আপনার পূর্ণ নাম সঠিকভাবে লিখুন (কমপক্ষে ২ অক্ষর)") }
                    return
                }
                val age = state.ageString.toIntOrNull()
                if (age == null || age !in 1..125) {
                    setState { copy(validationErrorBn = "১ থেকে ১২৫ এর মধ্যে সঠিক বয়স লিখুন") }
                    return
                }
                setState { copy(currentStep = 2, validationErrorBn = null) }
            }
            2 -> {
                // Step 2 (Gender & Blood Group) is complete or optional for blood group
                setState { copy(currentStep = 3, validationErrorBn = null) }
            }
            3 -> {
                saveProfile()
            }
        }
    }

    private fun handlePreviousStep() {
        val state = uiState.value
        if (state.currentStep > 1) {
            setState { copy(currentStep = state.currentStep - 1, validationErrorBn = null) }
        }
    }

    private fun saveProfile() {
        val state = uiState.value
        viewModelScope.launch {
            setState { copy(isSaving = true) }

            val combinedConditions = state.selectedChronicConditions.toMutableSet()
            if (state.otherCondition.isNotBlank()) {
                combinedConditions.add(state.otherCondition.trim())
            }

            val result = saveProfileUseCase(
                name = state.fullName,
                ageString = state.ageString,
                gender = state.selectedGender,
                bloodGroup = state.selectedBloodGroup,
                conditions = combinedConditions
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
