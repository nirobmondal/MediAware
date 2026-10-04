package com.example.mediaware.features.history.domain.usecase

import com.example.mediaware.features.history.domain.engine.LongitudinalNarrativeEngine
import com.example.mediaware.features.history.domain.model.NarrativeTrend
import com.example.mediaware.features.history.domain.model.VitalRecord
import com.example.mediaware.features.history.domain.model.VitalType
import javax.inject.Inject

data class VitalsTimelineResult(
    val records: List<VitalRecord>,
    val trend: NarrativeTrend?
)

class GetVitalsTimelineUseCase @Inject constructor() {

    operator fun invoke(vitalType: VitalType): VitalsTimelineResult {
        val records = getDefaultRecordsForType(vitalType)
        val isHigherBetter = vitalType == VitalType.HBA1C // None of these is typically higher better, but HbA1c/Sugar lower is better
        val trend = LongitudinalNarrativeEngine.computeTrend(records, isHigherBetter = false)
        return VitalsTimelineResult(records = records, trend = trend)
    }

    private fun getDefaultRecordsForType(vitalType: VitalType): List<VitalRecord> {
        val now = System.currentTimeMillis()
        val oneMonth = 30L * 24L * 60L * 60L * 1000L
        return when (vitalType) {
            VitalType.BLOOD_SUGAR -> listOf(
                VitalRecord("s1", VitalType.BLOOD_SUGAR, 110.0, now - 6 * oneMonth, "মার্চ ২০২৬", "খালি পেটে পরিমাপ"),
                VitalRecord("s2", VitalType.BLOOD_SUGAR, 125.0, now - 3 * oneMonth, "জুন ২০২৬", "খাবার ২ ঘণ্টা পর"),
                VitalRecord("s3", VitalType.BLOOD_SUGAR, 142.0, now, "অক্টোবর ২০২৬", "খালি পেটে পরিমাপ")
            )
            VitalType.BLOOD_PRESSURE_SYS -> listOf(
                VitalRecord("bp1", VitalType.BLOOD_PRESSURE_SYS, 145.0, now - 6 * oneMonth, "মার্চ ২০২৬", "বিশ্রামরত অবস্থায়"),
                VitalRecord("bp2", VitalType.BLOOD_PRESSURE_SYS, 134.0, now - 3 * oneMonth, "জুন ২০২৬", "সকালের পরিমাপ"),
                VitalRecord("bp3", VitalType.BLOOD_PRESSURE_SYS, 122.0, now, "অক্টোবর ২০২৬", "নিয়মিত ওষুধ সেবন পর")
            )
            VitalType.BLOOD_PRESSURE_DIA -> listOf(
                VitalRecord("bpd1", VitalType.BLOOD_PRESSURE_DIA, 95.0, now - 6 * oneMonth, "মার্চ ২০২৬", "বিশ্রামরত অবস্থায়"),
                VitalRecord("bpd2", VitalType.BLOOD_PRESSURE_DIA, 88.0, now - 3 * oneMonth, "জুন ২০২৬", "সকালের পরিমাপ"),
                VitalRecord("bpd3", VitalType.BLOOD_PRESSURE_DIA, 80.0, now, "অক্টোবর ২০২৬", "নিয়মিত ওষুধ সেবন পর")
            )
            VitalType.HBA1C -> listOf(
                VitalRecord("h1", VitalType.HBA1C, 7.8, now - 6 * oneMonth, "মার্চ ২০২৬", "ল্যাব রিপোর্ট"),
                VitalRecord("h2", VitalType.HBA1C, 7.2, now - 3 * oneMonth, "জুন ২০২৬", "ল্যাব রিপোর্ট"),
                VitalRecord("h3", VitalType.HBA1C, 6.8, now, "অক্টোবর ২০২৬", "সাম্প্রতিক টেস্ট")
            )
        }
    }
}
