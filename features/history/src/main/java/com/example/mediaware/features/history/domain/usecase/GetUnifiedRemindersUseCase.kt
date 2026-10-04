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
        val reminders = listOf(
            UnifiedReminder(
                id = "rem_1",
                title = "সেক্লো ২০ মি.গ্রা.",
                subtitle = "খাবারের ৩০ মিনিট পূর্বে সেব্য",
                type = ReminderType.MEDICINE,
                timeOfDayMinutes = 8 * 60, // 08:00 AM (480 mins)
                timeFormattedBn = "সকাল ০৮:০০",
                isEnabled = true
            ),
            UnifiedReminder(
                id = "rem_2",
                title = "ল্যাব টেস্ট ফাস্টিং শুরু",
                subtitle = "রক্তে সুগার ও লিপিড প্রোফাইল টেস্টের জন্য ৮ ঘণ্টা ফাস্টিং",
                type = ReminderType.LAB_TEST,
                timeOfDayMinutes = 8 * 60 + 5, // 08:05 AM (485 mins) - CONFLICT with rem_1 (<10 mins)!
                timeFormattedBn = "সকাল ০৮:০৫",
                isEnabled = true
            ),
            UnifiedReminder(
                id = "rem_3",
                title = "কমেট ৫০০ মি.গ্রা.",
                subtitle = "দুপুরের খাবার পর সেব্য",
                type = ReminderType.MEDICINE,
                timeOfDayMinutes = 13 * 60 + 30, // 01:30 PM (810 mins)
                timeFormattedBn = "দুপুর ০১:৩০",
                isEnabled = true
            ),
            UnifiedReminder(
                id = "rem_4",
                title = "কমেট ৫০০ মি.গ্রা.",
                subtitle = "রাতের খাবার পর সেব্য",
                type = ReminderType.MEDICINE,
                timeOfDayMinutes = 20 * 60 + 30, // 08:30 PM (1230 mins)
                timeFormattedBn = "রাত ০৮:৩০",
                isEnabled = true
            ),
            UnifiedReminder(
                id = "rem_5",
                title = "ডাঃ প্রফেসর এ. কে. আজাদ চেম্বার ফলো-আপ",
                subtitle = "৩ মাস পর ডায়াবেটিস ও রক্তচাপ পর্যালোচনা ভিজিট",
                type = ReminderType.CONSULTATION,
                timeOfDayMinutes = 10 * 60, // 10:00 AM
                timeFormattedBn = "সকাল ১০:০০ (৩ মাস পর)",
                isEnabled = true
            )
        )

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
