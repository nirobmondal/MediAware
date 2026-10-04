package com.example.mediaware.features.auth.presentation.setup

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.core.model.Gender

data class ProfileSetupUiState(
    val fullName: String = "",
    val ageString: String = "",
    val selectedGender: Gender = Gender.MALE,
    val selectedBloodGroup: String? = null,
    val selectedChronicConditions: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val validationErrorBn: String? = null
) : ViewState

sealed interface ProfileSetupUiEvent : ViewEvent {
    data class OnNameChanged(val name: String) : ProfileSetupUiEvent
    data class OnAgeChanged(val age: String) : ProfileSetupUiEvent
    data class OnGenderSelected(val gender: Gender) : ProfileSetupUiEvent
    data class OnBloodGroupSelected(val bloodGroup: String) : ProfileSetupUiEvent
    data class OnToggleChronicCondition(val conditionKey: String) : ProfileSetupUiEvent
    data object SaveProfile : ProfileSetupUiEvent
}

sealed interface ProfileSetupSideEffect : ViewSideEffect {
    data object NavigateToHome : ProfileSetupSideEffect
}
