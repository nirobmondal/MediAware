package com.example.mediaware.features.symptom.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.mediaware.features.symptom.domain.repository.FastingAlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FastingAlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FastingAlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    override fun scheduleFastingAlarm(
        testNameBn: String,
        fastingHours: Int,
        triggerEpochMillis: Long,
        reminderMessageBn: String
    ): Boolean {
        if (alarmManager == null) {
            Timber.e("AlarmManager service not available on device")
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Timber.w("Exact alarm permission not granted by user")
                // Fallback: Still attempt to schedule inexact or return status
            }
        }

        return try {
            val intent = Intent("com.example.mediaware.action.FASTING_ALARM").apply {
                setPackage(context.packageName)
                putExtra("EXTRA_TEST_NAME", testNameBn)
                putExtra("EXTRA_MESSAGE", reminderMessageBn)
                putExtra("EXTRA_HOURS", fastingHours)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                testNameBn.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            }
            Timber.i("Fasting alarm scheduled successfully for $testNameBn at epoch $triggerEpochMillis")
            true
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException while scheduling exact fasting alarm")
            false
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error scheduling fasting alarm")
            false
        }
    }

    override fun cancelFastingAlarm(testNameBn: String) {
        if (alarmManager == null) return
        try {
            val intent = Intent("com.example.mediaware.action.FASTING_ALARM").apply {
                setPackage(context.packageName)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                testNameBn.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Timber.i("Cancelled fasting alarm for $testNameBn")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling fasting alarm for $testNameBn")
        }
    }
}
