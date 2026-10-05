package com.example.mediaware.features.symptom.domain.model

data class Symptom(
    val id: String,
    val nameBn: String,
    val anatomicalRegionBn: String,
    val isRedFlagPotential: Boolean,
    val iconName: String
)

enum class Urgency {
    IMMEDIATE_999,
    IMMEDIATE_HOSPITAL,
    NORMAL
}

data class EmergencyTriageResult(
    val isEmergency: Boolean,
    val urgency: Urgency,
    val reasonBn: String,
    val matchedSymptoms: Set<String>
)

data class VisitPrepCard(
    val demographicsSummaryBn: String,
    val suggestedSpecialistBn: String,
    val severityScaleBn: String,
    val durationBn: String,
    val chiefComplaintsSummaryBn: String,
    val doctorSpeakingPointsBn: List<String>,
    val doctorQuestionsBn: List<String>,
    val mandatoryDisclaimerBn: String = "এটি কোনো প্রেসক্রিপশন নয়। যেকোনো সিদ্ধান্তে ডাক্তারের পরামর্শ নিন।"
)

data class FastingGuideline(
    val testId: String,
    val testNameBn: String,
    val testNameEn: String,
    val recommendedFastingHours: Int,
    val waterPermitted: Boolean,
    val guidancePointsBn: List<String>,
    val eveningAlertTextBn: String
)
