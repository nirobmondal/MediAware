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
        val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        fun Int.toBn(): String = this.toString().map { if (it in '0'..'9') bengaliDigits[it - '0'] else it }.joinToString("")

        val calendar = java.util.Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val calendarDays = (1..daysInMonth).map { day ->
            AdherenceDay(
                dayOfMonth = day,
                dayOfMonthBn = day.toBn(),
                status = AdherenceStatus.TAKEN
            )
        }

        return AdherenceHistoryResult(
            adherencePercentage = 0,
            totalDosesScheduled = 0,
            dosesTaken = 0,
            dosesMissed = 0,
            dosesSnoozed = 0,
            calendarDays = calendarDays,
            todayRecords = emptyList(),
            titrationHistory = emptyList()
        )
    }
}
