package com.example.mediaware.core.common.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowUpReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleFollowUpReminder(
        consultationId: String,
        doctorName: String,
        followUpDateMillis: Long,
        followUpReason: String? = null
    ) {
        val intent = Intent(context, FollowUpReminderReceiver::class.java).apply {
            action = FollowUpReminderReceiver.ACTION_FOLLOW_UP_REMINDER
            putExtra(FollowUpReminderReceiver.EXTRA_CONSULTATION_ID, consultationId)
            putExtra(FollowUpReminderReceiver.EXTRA_DOCTOR_NAME, doctorName)
            putExtra(FollowUpReminderReceiver.EXTRA_REASON, followUpReason ?: "পরবর্তী ফলো-আপ পরামর্শ")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            consultationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerCal = Calendar.getInstance().apply {
            timeInMillis = followUpDateMillis
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val triggerTime = if (triggerCal.timeInMillis <= System.currentTimeMillis()) {
            System.currentTimeMillis() + 10_000L
        } else {
            triggerCal.timeInMillis
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Timber.d("Follow-up reminder scheduled for $consultationId at $triggerTime")
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule follow-up reminder for $consultationId")
        }
    }

    fun cancelFollowUpReminder(consultationId: String) {
        val intent = Intent(context, FollowUpReminderReceiver::class.java).apply {
            action = FollowUpReminderReceiver.ACTION_FOLLOW_UP_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            consultationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Timber.d("Follow-up reminder cancelled for $consultationId")
    }
}
