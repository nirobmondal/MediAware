package com.example.mediaware.features.alarm.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mediaware.features.alarm.AlarmAlertActivity
import timber.log.Timber

/**
 * Full-Screen Alarm BroadcastReceiver.
 *
 * Receives the AlarmManager broadcast for a scheduled dose reminder and posts
 * a MAX-priority Heads-Up notification with a [fullScreenIntent] that launches
 * [AlarmAlertActivity] (Screen 31) directly over the device lock screen.
 *
 * This receiver does NOT perform any network calls — it is a synchronous,
 * on-device action satisfying Clinical Safety Guardrail 4.
 *
 * Registration: AndroidManifest.xml in :features:alarm with action
 * "com.example.mediaware.ALARM_TRIGGER_FULLSCREEN"
 */
class AlarmBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TITLE_BN = "extra_title_bn"
        const val EXTRA_INSTRUCTION_BN = "extra_instruction_bn"
        private const val CHANNEL_ID = "mediaware_fullscreen_alarm_channel"
        private const val NOTIFICATION_GROUP = "com.example.mediaware.DOSE_ALARMS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: "dose_reminder"
        val titleBn = intent.getStringExtra(EXTRA_TITLE_BN) ?: "ওষুধ গ্রহণের সময় হয়েছে"
        val instructionBn = intent.getStringExtra(EXTRA_INSTRUCTION_BN)
            ?: "ডাক্তারের নির্দেশিত ওষুধ সেবন করুন।"

        Timber.d("AlarmBroadcastReceiver.onReceive — reminderId=$reminderId")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        ensureNotificationChannel(notificationManager)

        // Intent to launch AlarmAlertActivity as a full-screen alarm overlay
        val fullScreenActivityIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_TITLE_BN, titleBn)
            putExtra(EXTRA_INSTRUCTION_BN, instructionBn)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            fullScreenActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Default alarm sound
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("💊 $titleBn")
            .setContentText(instructionBn)
            .setStyle(NotificationCompat.BigTextStyle().bigText(instructionBn))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(alarmSoundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setAutoCancel(false)
            .setOngoing(true)
            // Full-screen intent — wakes the screen even when locked
            .setFullScreenIntent(fullScreenPendingIntent, /* highPriority = */ true)
            .setContentIntent(fullScreenPendingIntent)
            .setGroup(NOTIFICATION_GROUP)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)
        Timber.d("Full-screen alarm notification posted for reminderId=$reminderId")
    }

    private fun ensureNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "ওষুধের অ্যালার্ম (Dose Alerts)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "লক-স্ক্রিনে ওষুধ গ্রহণের সময়মতো সতর্কবার্তা প্রদর্শন করে"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                    enableLights(true)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    setBypassDnd(true)
                }
                notificationManager.createNotificationChannel(channel)
                Timber.d("Alarm notification channel created")
            }
        }
    }
}
