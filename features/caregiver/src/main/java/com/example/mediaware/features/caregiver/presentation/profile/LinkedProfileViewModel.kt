package com.example.mediaware.features.caregiver.presentation.profile

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.core.common.di.IoDispatcher
import com.example.mediaware.features.caregiver.domain.model.CaregiverPermissions
import com.example.mediaware.features.caregiver.domain.model.LinkedPatientProfile
import com.example.mediaware.features.caregiver.domain.usecase.GetLinkedCaregiversUseCase
import com.example.mediaware.features.caregiver.domain.usecase.GetLinkedProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LinkedProfileUiState(
    val patientProfile: LinkedPatientProfile? = null,
    val permissions: CaregiverPermissions = CaregiverPermissions(CaregiverPermissions.PERM_REPORTS or CaregiverPermissions.PERM_DOSES or CaregiverPermissions.PERM_DOCTOR_VISITS),
    val isLoading: Boolean = true
) : ViewState

sealed interface LinkedProfileUiEvent : ViewEvent {
    data object RefreshProfile : LinkedProfileUiEvent
    data object CallPatientEmergency : LinkedProfileUiEvent
}

sealed interface LinkedProfileSideEffect : ViewSideEffect {
    data class DialEmergencyPhone(val phone: String) : LinkedProfileSideEffect
    data class ShowSnackbar(val message: String) : LinkedProfileSideEffect
}

@HiltViewModel
class LinkedProfileViewModel @Inject constructor(
    private val getLinkedProfileUseCase: GetLinkedProfileUseCase,
    private val getLinkedCaregiversUseCase: GetLinkedCaregiversUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel<LinkedProfileUiState, LinkedProfileUiEvent, LinkedProfileSideEffect>(
    LinkedProfileUiState()
) {

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch(ioDispatcher) {
            // Check active caregiver's permission mask
            getLinkedCaregiversUseCase().collect { caregivers ->
                val activeCaregiver = caregivers.firstOrNull { it.status.name != "REVOKED" }
                val perms = activeCaregiver?.permissions ?: CaregiverPermissions(
                    CaregiverPermissions.PERM_REPORTS or CaregiverPermissions.PERM_DOSES or CaregiverPermissions.PERM_DOCTOR_VISITS
                )

                getLinkedProfileUseCase(permissions = perms).collect { profile ->
                    setState {
                        copy(
                            patientProfile = profile,
                            permissions = perms,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    override fun onEvent(event: LinkedProfileUiEvent) {
        when (event) {
            is LinkedProfileUiEvent.RefreshProfile -> loadProfile()
            is LinkedProfileUiEvent.CallPatientEmergency -> {
                uiState.value.patientProfile?.emergencyPhone?.let { phone ->
                    sendEffect(LinkedProfileSideEffect.DialEmergencyPhone(phone))
                }
            }
        }
    }
}
