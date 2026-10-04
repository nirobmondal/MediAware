package com.example.mediaware.features.caregiver.domain.repository

import com.example.mediaware.features.caregiver.domain.model.CaregiverLink
import com.example.mediaware.features.caregiver.domain.model.CaregiverPermissions
import com.example.mediaware.features.caregiver.domain.model.LinkedPatientProfile
import kotlinx.coroutines.flow.Flow

interface CaregiverRepository {
    fun getCaregiverLinks(patientId: String): Flow<List<CaregiverLink>>
    suspend fun updatePermissions(linkId: String, permissions: CaregiverPermissions)
    suspend fun revokeCaregiver(linkId: String)
    suspend fun addCaregiver(link: CaregiverLink)
    fun getLinkedProfile(patientId: String, permissions: CaregiverPermissions): Flow<LinkedPatientProfile>
}
