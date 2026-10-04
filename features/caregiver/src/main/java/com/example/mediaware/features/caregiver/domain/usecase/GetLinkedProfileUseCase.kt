package com.example.mediaware.features.caregiver.domain.usecase

import com.example.mediaware.features.caregiver.domain.model.CaregiverPermissions
import com.example.mediaware.features.caregiver.domain.model.LinkedPatientProfile
import com.example.mediaware.features.caregiver.domain.repository.CaregiverRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLinkedProfileUseCase @Inject constructor(
    private val repository: CaregiverRepository
) {
    operator fun invoke(
        patientId: String = "user_primary",
        permissions: CaregiverPermissions = CaregiverPermissions(CaregiverPermissions.PERM_REPORTS or CaregiverPermissions.PERM_DOSES or CaregiverPermissions.PERM_DOCTOR_VISITS)
    ): Flow<LinkedPatientProfile> {
        return repository.getLinkedProfile(patientId, permissions)
    }
}
