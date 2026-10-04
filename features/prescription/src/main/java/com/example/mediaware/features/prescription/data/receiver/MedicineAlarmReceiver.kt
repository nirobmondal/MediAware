package com.example.mediaware.features.prescription.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import timber.log.Timber

class MedicineAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: "dose_reminder"
        val titleBn = intent.getStringExtra(EXTRA_TITLE_BN) ?: "ওষুধ গ্রহণের সময় হয়েছে"
        val instructionBn = intent.getStringExtra(EXTRA_INSTRUCTION_BN) ?: "ডাক্তারের নির্দেশিত ওষুধ সেবন করুন।"

        Timber.d("Medicine Alarm fired for reminderId=$reminderId: $titleBn - $instructionBn")

        showAlarmNotification(context, reminderId, titleBn, instructionBn)
    }

    private fun showAlarmNotification(
        context: Context,
        reminderId: String,
        titleBn: String,
        instructionBn: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mediaware_dose_alarm_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ওষুধের অ্যালার্ম (Dose Reminders)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "প্রেসক্রিপশন অনুযায়ী দৈনিক ওষুধ গ্রহণের সতর্কবার্তা"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Launch app intent on notification click
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titleBn)
            .setContentText(instructionBn)
            .setStyle(NotificationCompat.BigTextStyle().bigText(instructionBn))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(defaultSound)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TITLE_BN = "extra_title_bn"
        const val EXTRA_INSTRUCTION_BN = "extra_instruction_bn"
    }
}
