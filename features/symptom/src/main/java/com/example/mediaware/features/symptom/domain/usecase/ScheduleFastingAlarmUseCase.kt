package com.example.mediaware.features.symptom.domain.usecase

import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.features.symptom.domain.repository.FastingAlarmScheduler
import java.util.Calendar
import javax.inject.Inject

class ScheduleFastingAlarmUseCase @Inject constructor(
    private val fastingAlarmScheduler: FastingAlarmScheduler
) {

    operator fun invoke(
        testNameBn: String,
        fastingHours: Int = 8,
        customTriggerMillis: Long? = null
    ): Resource<Long> {
        return try {
            val triggerTime = customTriggerMillis ?: run {
                // Default: Calculate 9:30 PM of current/target evening or 8 hours from now
                val calendar = Calendar.getInstance()
                // If it is already late night (e.g. past 9:30 PM), schedule 8 hours ahead
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 21 && calendar.get(Calendar.MINUTE) >= 30) {
                    calendar.add(Calendar.HOUR_OF_DAY, fastingHours)
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, 21)
                    calendar.set(Calendar.MINUTE, 30)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    // If target 9:30 PM has passed today, move to tomorrow
                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                calendar.timeInMillis
            }

            val alertMessage = "$testNameBn টেস্টের প্রস্তুতি: টেস্টের আগে পানি ব্যতীত অন্য কোনো খাবার বা চা-কফি গ্রহণ করবেন না।"

            val success = fastingAlarmScheduler.scheduleFastingAlarm(
                testNameBn = testNameBn,
                fastingHours = fastingHours,
                triggerEpochMillis = triggerTime,
                reminderMessageBn = alertMessage
            )

            if (success) {
                Resource.Success(triggerTime)
            } else {
                Resource.Error("অ্যালার্ম সিডিউল করার অনুমতি পাওয়া যায়নি। দয়া করে অ্যালার্ম পারমিশন দিন।")
            }
        } catch (e: Exception) {
            Resource.Error("অ্যালার্ম সেট করতে সমস্যা হয়েছে: ${e.localizedMessage}")
        }
    }
}
