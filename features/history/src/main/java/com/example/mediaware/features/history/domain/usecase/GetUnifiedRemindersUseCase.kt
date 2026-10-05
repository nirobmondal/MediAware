package com.example.mediaware.features.history.domain.usecase

import com.example.mediaware.features.history.domain.model.ReminderConflict
import com.example.mediaware.features.history.domain.model.ReminderType
import com.example.mediaware.features.history.domain.model.UnifiedReminder
import javax.inject.Inject
import kotlin.math.abs

data class UnifiedRemindersResult(
    val reminders: List<UnifiedReminder>,
    val conflicts: List<ReminderConflict>
)

class GetUnifiedRemindersUseCase @Inject constructor() {

    operator fun invoke(): UnifiedRemindersResult {
        val reminders = emptyList<UnifiedReminder>()
        val conflicts = detectConflicts(reminders.filter { it.isEnabled })

        return UnifiedRemindersResult(
            reminders = reminders,
            conflicts = conflicts
        )
    }

    /**
     * Conflict Detection Engine:
     * Evaluates active reminders and flags schedule collisions where two alarms occur
     * within less than 10 minutes of each other (|diff| < 10 mins).
     */
    fun detectConflicts(activeReminders: List<UnifiedReminder>): List<ReminderConflict> {
        val conflicts = mutableListOf<ReminderConflict>()
        val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        fun Int.toBn(): String = this.toString().map { if (it in '0'..'9') bengaliDigits[it - '0'] else it }.joinToString("")

        for (i in 0 until activeReminders.size) {
            for (j in i + 1 until activeReminders.size) {
                val r1 = activeReminders[i]
                val r2 = activeReminders[j]
                val diff = abs(r1.timeOfDayMinutes - r2.timeOfDayMinutes)
                if (diff < 10) {
                    val message = "⚠️ সময় সংঘাত: '${r1.title}' (${r1.timeFormattedBn}) এবং '${r2.title}' (${r2.timeFormattedBn}) মাত্র ${diff.toBn()} মিনিটের ব্যবধানে নির্ধারিত রয়েছে। অ্যালার্ম ক্লান্তি এড়াতে সময় সমন্বয় করুন।"
                    conflicts.add(
                        ReminderConflict(
                            reminder1 = r1,
                            reminder2 = r2,
                            minuteDiff = diff,
                            messageBn = message
                        )
                    )
                }
            }
        }
        return conflicts
    }
}
