package com.example.mediaware.features.history.domain.model

enum class VitalType(
    val labelBn: String,
    val unit: String,
    val normalMin: Double,
    val normalMax: Double
) {
    BLOOD_SUGAR("রক্তের সুগার", "mg/dL", 70.0, 140.0),
    BLOOD_PRESSURE_SYS("সিস্টোলিক চাপ", "mmHg", 90.0, 120.0),
    BLOOD_PRESSURE_DIA("ডায়াস্টোলিক চাপ", "mmHg", 60.0, 80.0),
    HBA1C("হিমোগ্লোবিন HbA1c", "%", 4.0, 6.0)
}

data class VitalRecord(
    val id: String,
    val vitalType: VitalType,
    val value: Double,
    val timestamp: Long,
    val dateFormattedBn: String,
    val notesBn: String = ""
)

data class NarrativeTrend(
    val testNameBn: String,
    val previousValue: Double,
    val currentValue: Double,
    val monthsElapsed: Int,
    val narrativeBn: String,
    val isImproving: Boolean,
    val directionBn: String
)

enum class AdherenceStatus(val labelBn: String) {
    TAKEN("গৃহীত"),
    MISSED("মিস"),
    SNOOZED("স্থগিত")
}

data class AdherenceDay(
    val dayOfMonth: Int,
    val dayOfMonthBn: String,
    val status: AdherenceStatus,
    val isCurrentMonth: Boolean = true
)

data class AdherenceRecord(
    val id: String,
    val medicineName: String,
    val doseSlotBn: String,
    val scheduledTimeMillis: Long,
    val scheduledTimeBn: String,
    val status: AdherenceStatus,
    val statusTimestamp: Long
)

data class TitrationRecord(
    val id: String,
    val medicineName: String,
    val previousDose: String,
    val newDose: String,
    val titrationDateBn: String,
    val doctorName: String,
    val reasonBn: String
)

enum class ReminderType(val labelBn: String) {
    MEDICINE("ওষুধ"),
    LAB_TEST("ল্যাব প্রস্তুতি"),
    CONSULTATION("ডাক্তার ভিজিট")
}

data class UnifiedReminder(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: ReminderType,
    val timeOfDayMinutes: Int, // Minutes from midnight (e.g. 480 for 08:00 AM)
    val timeFormattedBn: String,
    val isEnabled: Boolean
)

data class ReminderConflict(
    val reminder1: UnifiedReminder,
    val reminder2: UnifiedReminder,
    val minuteDiff: Int,
    val messageBn: String
)
