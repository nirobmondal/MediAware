package com.example.mediaware.features.prescription.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.mediaware.features.prescription.data.receiver.MedicineAlarmReceiver
import com.example.mediaware.features.prescription.domain.repository.DoseAlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class DoseAlarmSchedulerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DoseAlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleExactDailyAlarm(
        reminderId: String,
        hour: Int,
        minute: Int,
        titleBn: String,
        instructionBn: String
    ) {
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = "com.example.mediaware.ALARM_TRIGGER"
            putExtra(MedicineAlarmReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(MedicineAlarmReceiver.EXTRA_TITLE_BN, titleBn)
            putExtra(MedicineAlarmReceiver.EXTRA_INSTRUCTION_BN, instructionBn)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1) // Target time already passed today, set for tomorrow
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Timber.d("Exact alarm scheduled while idle for $reminderId at ${calendar.time}")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Timber.w("SCHEDULE_EXACT_ALARM not allowed; fallback to setAndAllowWhileIdle for $reminderId")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Timber.d("Exact alarm scheduled for $reminderId at ${calendar.time}")
            }
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException while scheduling exact alarm for $reminderId")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    override fun cancelAlarm(reminderId: String) {
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = "com.example.mediaware.ALARM_TRIGGER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Timber.d("Canceled alarm for $reminderId")
    }
}
