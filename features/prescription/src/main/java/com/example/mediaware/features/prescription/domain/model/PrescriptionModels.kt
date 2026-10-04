package com.example.mediaware.features.prescription.domain.model

import java.util.UUID

data class PrescriptionItem(
    val id: String = UUID.randomUUID().toString(),
    val brandName: String,
    val genericName: String = "",
    val strength: String = "",
    val form: String = "ট্যাবলেট", // ট্যাবলেট, ক্যাপসুল, সিরাপ
    val rawDosagePattern: String = "1+0+1",
    val rawMealTiming: String? = "PC",
    val durationDays: Int = 7,
    val morningQty: Int = 1,
    val noonQty: Int = 0,
    val nightQty: Int = 1,
    val timingSlotBn: String = "সকালে ও রাতে (দিনে ২ বার)",
    val mealInstructionBn: String = "ভরা পেটে বা খাবারের পর সেব্য"
)

data class MedicineExplanation(
    val genericName: String,
    val brandName: String = "",
    val banglaName: String,
    val therapeuticClass: String,
    val primaryPurposeBn: String,
    val standardDosageBn: String,
    val sideEffectsBn: String? = null,
    val criticalWarningsBn: String,
    val lifestylePrecautionsBn: String? = null,
    val isFromCache: Boolean = false,
    val mandatoryDisclaimerBn: String = "⚠️ এটি কোনো প্রেসক্রিপশন নয়। যেকোনো সিদ্ধান্তে ডাক্তারের পরামর্শ নিন।"
)

enum class DoseSlot(val labelBn: String, val defaultHour: Int, val defaultMinute: Int, val defaultTimeBn: String) {
    MORNING("সকাল", 8, 0, "সকাল ০৮:০০"),
    NOON("দুপুর", 13, 30, "দুপুর ০১:৩০"),
    NIGHT("রাত", 20, 30, "রাত ০৮:৩০")
}

data class SlotSchedule(
    val slot: DoseSlot,
    val timeStringBn: String,
    val hour: Int,
    val minute: Int,
    val medicines: List<PrescriptionItem>,
    val isEnabled: Boolean = true
)
