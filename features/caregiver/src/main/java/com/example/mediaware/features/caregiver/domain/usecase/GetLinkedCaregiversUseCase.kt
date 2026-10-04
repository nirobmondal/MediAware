package com.example.mediaware.features.caregiver.domain.usecase

import com.example.mediaware.features.caregiver.domain.model.CaregiverLink
import com.example.mediaware.features.caregiver.domain.repository.CaregiverRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLinkedCaregiversUseCase @Inject constructor(
    private val repository: CaregiverRepository
) {
    operator fun invoke(patientId: String = "user_primary"): Flow<List<CaregiverLink>> {
        return repository.getCaregiverLinks(patientId)
    }
}
