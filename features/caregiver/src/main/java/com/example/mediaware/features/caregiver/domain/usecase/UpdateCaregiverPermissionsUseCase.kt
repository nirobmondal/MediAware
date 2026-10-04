package com.example.mediaware.features.caregiver.domain.usecase

import com.example.mediaware.features.caregiver.domain.model.CaregiverPermissions
import com.example.mediaware.features.caregiver.domain.repository.CaregiverRepository
import javax.inject.Inject

class UpdateCaregiverPermissionsUseCase @Inject constructor(
    private val repository: CaregiverRepository
) {
    suspend operator fun invoke(linkId: String, permissions: CaregiverPermissions) {
        repository.updatePermissions(linkId, permissions)
    }
}
