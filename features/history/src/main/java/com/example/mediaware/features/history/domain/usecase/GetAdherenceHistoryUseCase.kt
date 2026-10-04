package com.example.mediaware.features.history.domain.usecase

import com.example.mediaware.features.history.domain.model.AdherenceDay
import com.example.mediaware.features.history.domain.model.AdherenceRecord
import com.example.mediaware.features.history.domain.model.AdherenceStatus
import com.example.mediaware.features.history.domain.model.TitrationRecord
import javax.inject.Inject

data class AdherenceHistoryResult(
    val adherencePercentage: Int,
    val totalDosesScheduled: Int,
    val dosesTaken: Int,
    val dosesMissed: Int,
    val dosesSnoozed: Int,
    val calendarDays: List<AdherenceDay>,
    val todayRecords: List<AdherenceRecord>,
    val titrationHistory: List<TitrationRecord>
)

class GetAdherenceHistoryUseCase @Inject constructor() {

    operator fun invoke(): AdherenceHistoryResult {
        val totalDoses = 60
        val taken = 51
        val missed = 6
        val snoozed = 3
        val percentage = ((taken.toFloat() / totalDoses) * 100).toInt()

        val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        fun Int.toBn(): String = this.toString().map { if (it in '0'..'9') bengaliDigits[it - '0'] else it }.joinToString("")

        // Generate 30 days for calendar view
        val calendarDays = (1..30).map { day ->
            val status = when {
                day in listOf(3, 11, 24) -> AdherenceStatus.MISSED
                day in listOf(7, 19) -> AdherenceStatus.SNOOZED
                else -> AdherenceStatus.TAKEN
            }
            AdherenceDay(
                dayOfMonth = day,
                dayOfMonthBn = day.toBn(),
                status = status
            )
        }

        val now = System.currentTimeMillis()
        val todayRecords = listOf(
            AdherenceRecord(
                id = "adh_1",
                medicineName = "সেক্লো ২০ মি.গ্রা. (ওমেপ্রাজল)",
                doseSlotBn = "সকাল (নাস্তার ৩০ মিনিট পূর্বে)",
                scheduledTimeMillis = now - 2 * 3600 * 1000L,
                scheduledTimeBn = "সকাল ০৮:০০",
                status = AdherenceStatus.TAKEN,
                statusTimestamp = now - 2 * 3600 * 1000L
            ),
            AdherenceRecord(
                id = "adh_2",
                medicineName = "কমেট ৫০০ মি.গ্রা. (মেটফরমিন)",
                doseSlotBn = "দুপুর (ভরা পেটে)",
                scheduledTimeMillis = now - 3600 * 1000L,
                scheduledTimeBn = "দুপুর ০১:৩০",
                status = AdherenceStatus.TAKEN,
                statusTimestamp = now - 3600 * 1000L
            ),
            AdherenceRecord(
                id = "adh_3",
                medicineName = "কমেট ৫০০ মি.গ্রা. (মেটফরমিন)",
                doseSlotBn = "রাত (ভরা পেটে)",
                scheduledTimeMillis = now + 3 * 3600 * 1000L,
                scheduledTimeBn = "রাত ০৮:৩০",
                status = AdherenceStatus.SNOOZED,
                statusTimestamp = 0L
            )
        )

        val titrationHistory = listOf(
            TitrationRecord(
                id = "tit_1",
                medicineName = "কমেট (মেটফরমিন)",
                previousDose = "৫০০ মি.গ্রা. (দিনে ১ বার)",
                newDose = "৮৫০ মি.গ্রা. (দিনে ২ বার)",
                titrationDateBn = "৪ অক্টোবর, ২০২৬",
                doctorName = "ডাঃ প্রফেসর এ. কে. আজাদ",
                reasonBn = "রক্তে সুগারের মাত্রা কিছুটা বৃদ্ধি পাওয়ায় ডোজ সমন্বয় করা হয়েছে।"
            ),
            TitrationRecord(
                id = "tit_2",
                medicineName = "ওসারটিল (লোসারটান)",
                previousDose = "২৫ মি.গ্রা. (সকালে)",
                newDose = "৫০ মি.গ্রা. (সকালে)",
                titrationDateBn = "১৫ জুন, ২০২৬",
                doctorName = "ডাঃ রাশেদুল ইসলাম",
                reasonBn = "উচ্চ রক্তচাপ নিয়ন্ত্রণে রাখার সুবিধার্থে।"
            )
        )

        return AdherenceHistoryResult(
            adherencePercentage = percentage,
            totalDosesScheduled = totalDoses,
            dosesTaken = taken,
            dosesMissed = missed,
            dosesSnoozed = snoozed,
            calendarDays = calendarDays,
            todayRecords = todayRecords,
            titrationHistory = titrationHistory
        )
    }
}
