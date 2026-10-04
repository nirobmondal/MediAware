package com.example.mediaware.features.caregiver.data.repository

import com.example.mediaware.core.common.di.IoDispatcher
import com.example.mediaware.core.database.dao.CaregiverDao
import com.example.mediaware.core.database.entity.CaregiverLinkEntity
import com.example.mediaware.features.caregiver.domain.model.*
import com.example.mediaware.features.caregiver.domain.repository.CaregiverRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaregiverRepositoryImpl @Inject constructor(
    private val caregiverDao: CaregiverDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CaregiverRepository {

    override fun getCaregiverLinks(patientId: String): Flow<List<CaregiverLink>> {
        return caregiverDao.getCaregiverLinksForPatient(patientId)
            .map { entities ->
                if (entities.isEmpty()) {
                    listOf(
                        CaregiverLink(
                            linkId = "link_cg_01",
                            patientUserId = patientId,
                            caregiverUserId = "user_cg_tanvir",
                            caregiverName = "তানভীর আহমেদ",
                            relationship = CaregiverRelationship.SON,
                            permissions = CaregiverPermissions(
                                CaregiverPermissions.PERM_REPORTS or
                                        CaregiverPermissions.PERM_DOSES or
                                        CaregiverPermissions.PERM_DOCTOR_VISITS
                            ),
                            status = CaregiverLinkStatus.ACTIVE,
                            createdAt = System.currentTimeMillis() - 86400000L * 7,
                            expiresAt = System.currentTimeMillis() + 86400000L * 365
                        ),
                        CaregiverLink(
                            linkId = "link_cg_02",
                            patientUserId = patientId,
                            caregiverUserId = "user_cg_farhana",
                            caregiverName = "ফারহানা করিম",
                            relationship = CaregiverRelationship.DAUGHTER,
                            permissions = CaregiverPermissions(
                                CaregiverPermissions.PERM_REPORTS or CaregiverPermissions.PERM_DOSES
                            ),
                            status = CaregiverLinkStatus.ACTIVE,
                            createdAt = System.currentTimeMillis() - 86400000L * 3,
                            expiresAt = System.currentTimeMillis() + 86400000L * 365
                        )
                    )
                } else {
                    entities
                        .filter { it.linkStatus != CaregiverLinkStatus.REVOKED.name }
                        .map { it.toDomain() }
                }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun updatePermissions(linkId: String, permissions: CaregiverPermissions) {
        withContext(ioDispatcher) {
            val existing = caregiverDao.getCaregiverLinkById(linkId)
            if (existing == null) {
                val seedName = if (linkId == "link_cg_01") "তানভীর আহমেদ" else "ফারহানা করিম"
                val seedRel = if (linkId == "link_cg_01") CaregiverRelationship.SON else CaregiverRelationship.DAUGHTER
                caregiverDao.insertOrUpdate(
                    CaregiverLinkEntity(
                        linkId = linkId,
                        patientUserId = "user_primary",
                        caregiverUserId = "user_cg_$linkId",
                        caregiverName = seedName,
                        pairingCodeHash = "hash_$linkId",
                        relationshipType = seedRel.name,
                        permissionsMask = permissions.mask,
                        linkStatus = CaregiverLinkStatus.ACTIVE.name,
                        createdAt = System.currentTimeMillis(),
                        expiresAt = System.currentTimeMillis() + 86400000L * 365
                    )
                )
            } else {
                caregiverDao.updatePermissions(linkId, permissions.mask)
            }
        }
    }

    override suspend fun revokeCaregiver(linkId: String) {
        withContext(ioDispatcher) {
            val existing = caregiverDao.getCaregiverLinkById(linkId)
            if (existing == null) {
                val seedName = if (linkId == "link_cg_01") "তানভীর আহমেদ" else "ফারহানা করিম"
                val seedRel = if (linkId == "link_cg_01") CaregiverRelationship.SON else CaregiverRelationship.DAUGHTER
                caregiverDao.insertOrUpdate(
                    CaregiverLinkEntity(
                        linkId = linkId,
                        patientUserId = "user_primary",
                        caregiverUserId = "user_cg_$linkId",
                        caregiverName = seedName,
                        pairingCodeHash = "hash_$linkId",
                        relationshipType = seedRel.name,
                        permissionsMask = 0,
                        linkStatus = CaregiverLinkStatus.REVOKED.name,
                        createdAt = System.currentTimeMillis(),
                        expiresAt = System.currentTimeMillis()
                    )
                )
            } else {
                caregiverDao.updateStatus(linkId, CaregiverLinkStatus.REVOKED.name)
            }
        }
    }

    override suspend fun addCaregiver(link: CaregiverLink) {
        withContext(ioDispatcher) {
            caregiverDao.insertOrUpdate(link.toEntity())
        }
    }

    override fun getLinkedProfile(
        patientId: String,
        permissions: CaregiverPermissions
    ): Flow<LinkedPatientProfile> = flow {
        val profile = LinkedPatientProfile(
            patientId = patientId,
            patientName = "হাজী আব্দুল কাদের",
            ageBn = "৬৮",
            genderBn = "পুরুষ",
            bloodGroup = "B+",
            emergencyPhone = "01711000000",
            relationshipBn = "পিতা",
            adherenceRateBn = "৮৫%",
            doses = listOf(
                LinkedDoseItem(
                    id = "dose_1",
                    medicineName = "সেক্লো ২০ মি.গ্রা. (ক্যাপসুল)",
                    scheduledTimeBn = "সকাল ০৮:০০",
                    instructionBn = "খাবারের ৩০ মিনিট পূর্বে সেব্য",
                    isTaken = true,
                    statusBn = "গ্রহণ করেছেন"
                ),
                LinkedDoseItem(
                    id = "dose_2",
                    medicineName = "কমেট ৫০০ মি.গ্রা. (ট্যাবলেট)",
                    scheduledTimeBn = "দুপুর ০১:৩০",
                    instructionBn = "দুপুরের খাবারের পর সেব্য",
                    isTaken = true,
                    statusBn = "গ্রহণ করেছেন"
                ),
                LinkedDoseItem(
                    id = "dose_3",
                    medicineName = "রোসুভা ১০ মি.গ্রা. (ট্যাবলেট)",
                    scheduledTimeBn = "রাত ০৯:০০",
                    instructionBn = "রাতের খাবারের পর সেব্য",
                    isTaken = false,
                    statusBn = "বাকি আছে"
                )
            ),
            vitals = listOf(
                LinkedVitalItem(
                    titleBn = "রক্তে সুগার (ফাস্টিং)",
                    valueBn = "৭.২ mmol/L",
                    statusBn = "স্বাভাবিকের চেয়ে কিছুটা বেশি",
                    dateBn = "আজ সকাল ০৮:৩০"
                ),
                LinkedVitalItem(
                    titleBn = "রক্তচাপ (BP)",
                    valueBn = "১৩০/৮৫ mmHg",
                    statusBn = "নিয়ন্ত্রণে আছে",
                    dateBn = "গতকাল বিকাল ০৫:০০"
                ),
                LinkedVitalItem(
                    titleBn = "এইচবিএওয়ানসি (HbA1c)",
                    valueBn = "৬.৮%",
                    statusBn = "পরিমিত নিয়ন্ত্রণ",
                    dateBn = "১৫ দিন আগে"
                )
            ),
            nextConsultation = LinkedConsultationItem(
                doctorName = "ডাঃ প্রফেসর এ. কে. আজাদ",
                specialty = "হৃদরোগ ও মেডিসিন বিশেষজ্ঞ",
                scheduledDateBn = "১৫ নভেম্বর, ২০২৬ (সকাল ১০:০০)",
                notesBn = "রক্তচাপ ও লিপিড প্রোফাইল রিপোর্ট সাথে আনবেন।"
            ),
            permissions = permissions
        )
        emit(profile)
    }.flowOn(ioDispatcher)

    private fun CaregiverLinkEntity.toDomain(): CaregiverLink {
        return CaregiverLink(
            linkId = linkId,
            patientUserId = patientUserId,
            caregiverUserId = caregiverUserId,
            caregiverName = caregiverName,
            relationship = runCatching { CaregiverRelationship.valueOf(relationshipType) }
                .getOrDefault(CaregiverRelationship.OTHER),
            permissions = CaregiverPermissions(permissionsMask),
            status = runCatching { CaregiverLinkStatus.valueOf(linkStatus) }
                .getOrDefault(CaregiverLinkStatus.PENDING),
            createdAt = createdAt,
            expiresAt = expiresAt
        )
    }

    private fun CaregiverLink.toEntity(): CaregiverLinkEntity {
        return CaregiverLinkEntity(
            linkId = linkId,
            patientUserId = patientUserId,
            caregiverUserId = caregiverUserId,
            caregiverName = caregiverName,
            pairingCodeHash = "hash_${linkId}",
            relationshipType = relationship.name,
            permissionsMask = permissions.mask,
            linkStatus = status.name,
            createdAt = createdAt,
            expiresAt = expiresAt
        )
    }
}
