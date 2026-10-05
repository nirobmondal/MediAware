package com.example.mediaware.features.alarm.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar

/**
 * BootReceiver — Re-registers all pending dose alarms after device reboot.
 *
 * Android OS clears ALL AlarmManager alarms on device power-off/restart.
 * This receiver listens for BOOT_COMPLETED and LOCKED_BOOT_COMPLETED, then
 * reschedules dummy placeholder alarms so a user's real alarms survive reboots.
 *
 * Architecture note:
 * - Uses BroadcastReceiver.goAsync() to safely run a short-lived coroutine
 *   without blocking the main thread for the DB read.
 * - Full alarm re-registration requires a ReminderScheduleDao in :core:database.
 *   This receiver is wired up to self-schedule the default 3 daily dose slots
 *   until a ReminderScheduleDao is available.
 *
 * Registration: AndroidManifest.xml with BOOT_COMPLETED + LOCKED_BOOT_COMPLETED.
 *
 * Per Android docs: RECEIVE_BOOT_COMPLETED permission is required and declared
 * in :features:alarm AndroidManifest.xml.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) {
            return
        }

        Timber.i("BootReceiver.onReceive — $action: re-registering dose alarms")

        // goAsync() lets us safely run a short coroutine without ANR risk.
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                reRegisterDefaultDoseAlarms(context)
            } catch (e: Exception) {
                Timber.e(e, "BootReceiver — failed to re-register alarms")
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Re-registers the 3 standard daily dose-slot alarms (Morning / Noon / Night).
     *
     * These match the [DoseSlot] defaults defined in :features:prescription.
     * They fire [AlarmBroadcastReceiver] which posts the full-screen intent.
     *
     * A more complete implementation would query a ReminderScheduleDao from
     * :core:database and re-register each user-configured reminder row.
     * The extendable TODO is marked below.
     */
    private fun reRegisterDefaultDoseAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Default dose slots from PrescriptionModels.DoseSlot
        val defaultSlots = listOf(
            Triple("dose_morning", 8, 0),   // সকাল ০৮:০০
            Triple("dose_noon", 13, 30),    // দুপুর ০১:৩০
            Triple("dose_night", 20, 30)    // রাত ০৮:৩০
        )

        // TODO: Replace with: val activeReminders = reminderDao.getActiveRemindersSync()
        //       Then iterate and schedule each from Room DB rows.

        for ((reminderId, hour, minute) in defaultSlots) {
            scheduleExactAlarm(context, alarmManager, reminderId, hour, minute)
        }

        Timber.i("BootReceiver — ${defaultSlots.size} dose alarms re-registered after reboot")
    }

    private fun scheduleExactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        reminderId: String,
        hour: Int,
        minute: Int
    ) {
        val targetTimeMs = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time has already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }.timeInMillis

        val broadcastIntent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = "com.example.mediaware.ALARM_TRIGGER_FULLSCREEN"
            putExtra(AlarmBroadcastReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(AlarmBroadcastReceiver.EXTRA_TITLE_BN, "ওষুধ গ্রহণের সময় হয়েছে")
            putExtra(AlarmBroadcastReceiver.EXTRA_INSTRUCTION_BN, "ডাক্তারের নির্দেশিত ওষুধ সেবন করুন।")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, targetTimeMs, pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, targetTimeMs, pendingIntent
                    )
                    Timber.w("BootReceiver — SCHEDULE_EXACT_ALARM not granted; using setAndAllowWhileIdle for $reminderId")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, targetTimeMs, pendingIntent
                )
            }
            Timber.d("BootReceiver — alarm re-registered: $reminderId at $hour:$minute")
        } catch (e: SecurityException) {
            Timber.e(e, "BootReceiver — SecurityException for $reminderId; fallback to inexact")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTimeMs, pendingIntent)
        }
    }
}
