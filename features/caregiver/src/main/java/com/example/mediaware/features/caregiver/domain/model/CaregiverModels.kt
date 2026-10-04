package com.example.mediaware.features.caregiver.domain.model

enum class CaregiverRelationship(val labelBn: String) {
    SON("ছেলে"),
    DAUGHTER("মেয়ে"),
    SPOUSE("জীবনসঙ্গী"),
    OTHER("অন্যান্য")
}

enum class CaregiverLinkStatus(val labelBn: String) {
    PENDING("অপেক্ষমাণ"),
    ACTIVE("সক্রিয়"),
    REVOKED("বাতিল"),
    EXPIRED("মেয়াদোত্তীর্ণ")
}

data class CaregiverPermissions(
    val mask: Int = PERM_REPORTS
) {
    val canViewReports: Boolean get() = (mask and PERM_REPORTS) != 0
    val canViewDoses: Boolean get() = (mask and PERM_DOSES) != 0
    val canViewDoctorVisits: Boolean get() = (mask and PERM_DOCTOR_VISITS) != 0

    fun copyWith(
        reports: Boolean = canViewReports,
        doses: Boolean = canViewDoses,
        doctorVisits: Boolean = canViewDoctorVisits
    ): CaregiverPermissions {
        var newMask = 0
        if (reports) newMask = newMask or PERM_REPORTS
        if (doses) newMask = newMask or PERM_DOSES
        if (doctorVisits) newMask = newMask or PERM_DOCTOR_VISITS
        return CaregiverPermissions(mask = newMask)
    }

    companion object {
        const val PERM_REPORTS = 1       // Bit 0: 2^0 = 1
        const val PERM_DOSES = 2         // Bit 1: 2^1 = 2
        const val PERM_DOCTOR_VISITS = 4 // Bit 2: 2^2 = 4

        fun fromFlags(
            reports: Boolean,
            doses: Boolean,
            doctorVisits: Boolean
        ): CaregiverPermissions {
            var m = 0
            if (reports) m = m or PERM_REPORTS
            if (doses) m = m or PERM_DOSES
            if (doctorVisits) m = m or PERM_DOCTOR_VISITS
            return CaregiverPermissions(mask = m)
        }
    }
}

data class PairingCode(
    val code: String,
    val expiresAt: Long,
    val ttlSeconds: Long = 600L,
    val formattedCodeBn: String,
    val deepLinkUri: String
)

data class CaregiverLink(
    val linkId: String,
    val patientUserId: String,
    val caregiverUserId: String,
    val caregiverName: String,
    val relationship: CaregiverRelationship,
    val permissions: CaregiverPermissions,
    val status: CaregiverLinkStatus,
    val createdAt: Long,
    val expiresAt: Long
)

data class LinkedDoseItem(
    val id: String,
    val medicineName: String,
    val scheduledTimeBn: String,
    val instructionBn: String,
    val isTaken: Boolean,
    val statusBn: String
)

data class LinkedVitalItem(
    val titleBn: String,
    val valueBn: String,
    val statusBn: String,
    val dateBn: String
)

data class LinkedConsultationItem(
    val doctorName: String,
    val specialty: String,
    val scheduledDateBn: String,
    val notesBn: String
)

data class LinkedPatientProfile(
    val patientId: String,
    val patientName: String,
    val ageBn: String,
    val genderBn: String,
    val bloodGroup: String,
    val emergencyPhone: String,
    val relationshipBn: String,
    val adherenceRateBn: String,
    val doses: List<LinkedDoseItem>,
    val vitals: List<LinkedVitalItem>,
    val nextConsultation: LinkedConsultationItem?,
    val permissions: CaregiverPermissions,
    val disclaimerBn: String = "⚠️ এটি কোনো প্রেসক্রিপশন নয়। যেকোনো সিদ্ধান্তে ডাক্তারের পরামর্শ নিন।"
)
