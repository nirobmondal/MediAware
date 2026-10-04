package com.example.mediaware.features.caregiver.presentation.manage

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.core.common.di.IoDispatcher
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.caregiver.domain.model.CaregiverLink
import com.example.mediaware.features.caregiver.domain.model.CaregiverPermissions
import com.example.mediaware.features.caregiver.domain.usecase.GetLinkedCaregiversUseCase
import com.example.mediaware.features.caregiver.domain.usecase.UpdateCaregiverPermissionsUseCase
import com.example.mediaware.features.caregiver.domain.repository.CaregiverRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CaregiverManageUiState(
    val caregivers: List<CaregiverLink> = emptyList(),
    val selectedCaregiver: CaregiverLink? = null,
    val permReports: Boolean = true,
    val permDoses: Boolean = true,
    val permDoctorVisits: Boolean = true,
    val bitmaskValue: Int = 7,
    val bitmaskBinaryBn: String = "০b১১১",
    val showRevokeDialog: Boolean = false,
    val isLoading: Boolean = true
) : ViewState

sealed interface CaregiverManageUiEvent : ViewEvent {
    data class SelectCaregiver(val caregiver: CaregiverLink) : CaregiverManageUiEvent
    data class TogglePermReports(val enabled: Boolean) : CaregiverManageUiEvent
    data class TogglePermDoses(val enabled: Boolean) : CaregiverManageUiEvent
    data class TogglePermDoctorVisits(val enabled: Boolean) : CaregiverManageUiEvent
    data object SavePermissions : CaregiverManageUiEvent
    data class ShowRevokeConfirmation(val show: Boolean) : CaregiverManageUiEvent
    data object ConfirmRevokeCaregiver : CaregiverManageUiEvent
}

sealed interface CaregiverManageSideEffect : ViewSideEffect {
    data class ShowSnackbar(val message: String) : CaregiverManageSideEffect
}

@HiltViewModel
class CaregiverManageViewModel @Inject constructor(
    private val getLinkedCaregiversUseCase: GetLinkedCaregiversUseCase,
    private val updateCaregiverPermissionsUseCase: UpdateCaregiverPermissionsUseCase,
    private val caregiverRepository: CaregiverRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel<CaregiverManageUiState, CaregiverManageUiEvent, CaregiverManageSideEffect>(
    CaregiverManageUiState()
) {

    init {
        loadCaregivers()
    }

    private fun loadCaregivers() {
        viewModelScope.launch(ioDispatcher) {
            getLinkedCaregiversUseCase().collect { list ->
                val activeList = list.filter { it.status.name != "REVOKED" }
                val current = uiState.value.selectedCaregiver
                val selected = if (current != null && activeList.any { it.linkId == current.linkId }) {
                    activeList.first { it.linkId == current.linkId }
                } else {
                    activeList.firstOrNull()
                }

                if (selected != null) {
                    val reports = selected.permissions.canViewReports
                    val doses = selected.permissions.canViewDoses
                    val visits = selected.permissions.canViewDoctorVisits
                    val mask = selected.permissions.mask
                    val binBn = "০b" + Integer.toBinaryString(mask).padStart(3, '0').toBengaliDigits()

                    setState {
                        copy(
                            caregivers = activeList,
                            selectedCaregiver = selected,
                            permReports = reports,
                            permDoses = doses,
                            permDoctorVisits = visits,
                            bitmaskValue = mask,
                            bitmaskBinaryBn = binBn,
                            isLoading = false
                        )
                    }
                } else {
                    setState {
                        copy(
                            caregivers = activeList,
                            selectedCaregiver = null,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    override fun onEvent(event: CaregiverManageUiEvent) {
        when (event) {
            is CaregiverManageUiEvent.SelectCaregiver -> {
                val reports = event.caregiver.permissions.canViewReports
                val doses = event.caregiver.permissions.canViewDoses
                val visits = event.caregiver.permissions.canViewDoctorVisits
                val mask = event.caregiver.permissions.mask
                val binBn = "০b" + Integer.toBinaryString(mask).padStart(3, '0').toBengaliDigits()
                setState {
                    copy(
                        selectedCaregiver = event.caregiver,
                        permReports = reports,
                        permDoses = doses,
                        permDoctorVisits = visits,
                        bitmaskValue = mask,
                        bitmaskBinaryBn = binBn
                    )
                }
            }
            is CaregiverManageUiEvent.TogglePermReports -> {
                val newReports = event.enabled
                updateStateMask(reports = newReports, doses = uiState.value.permDoses, visits = uiState.value.permDoctorVisits)
            }
            is CaregiverManageUiEvent.TogglePermDoses -> {
                val newDoses = event.enabled
                updateStateMask(reports = uiState.value.permReports, doses = newDoses, visits = uiState.value.permDoctorVisits)
            }
            is CaregiverManageUiEvent.TogglePermDoctorVisits -> {
                val newVisits = event.enabled
                updateStateMask(reports = uiState.value.permReports, doses = uiState.value.permDoses, visits = newVisits)
            }
            is CaregiverManageUiEvent.SavePermissions -> {
                val current = uiState.value.selectedCaregiver ?: return
                val newPerms = CaregiverPermissions.fromFlags(
                    reports = uiState.value.permReports,
                    doses = uiState.value.permDoses,
                    doctorVisits = uiState.value.permDoctorVisits
                )
                viewModelScope.launch(ioDispatcher) {
                    updateCaregiverPermissionsUseCase(current.linkId, newPerms)
                    sendEffect(CaregiverManageSideEffect.ShowSnackbar("কেয়ারগিভার '${current.caregiverName}'-এর অনুমতি সফলভাবে সংরক্ষিত হয়েছে"))
                }
            }
            is CaregiverManageUiEvent.ShowRevokeConfirmation -> {
                setState { copy(showRevokeDialog = event.show) }
            }
            is CaregiverManageUiEvent.ConfirmRevokeCaregiver -> {
                val current = uiState.value.selectedCaregiver ?: return
                setState { copy(showRevokeDialog = false) }
                viewModelScope.launch(ioDispatcher) {
                    caregiverRepository.revokeCaregiver(current.linkId)
                    sendEffect(CaregiverManageSideEffect.ShowSnackbar("কেয়ারগিভার '${current.caregiverName}'-এর সংযোগ বিচ্ছিন্ন করা হয়েছে"))
                }
            }
        }
    }

    private fun updateStateMask(reports: Boolean, doses: Boolean, visits: Boolean) {
        val newMask = CaregiverPermissions.fromFlags(reports, doses, visits).mask
        val binBn = "০b" + Integer.toBinaryString(newMask).padStart(3, '0').toBengaliDigits()
        setState {
            copy(
                permReports = reports,
                permDoses = doses,
                permDoctorVisits = visits,
                bitmaskValue = newMask,
                bitmaskBinaryBn = binBn
            )
        }
    }
}
