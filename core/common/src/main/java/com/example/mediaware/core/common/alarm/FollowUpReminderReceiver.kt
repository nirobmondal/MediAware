package com.example.mediaware.core.common.alarm

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

/**
 * BroadcastReceiver triggered by AlarmManager on the scheduled follow-up visit date.
 * Posts an official MediAware Follow-Up Reminder Notification alerting the patient.
 */
class FollowUpReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val consultationId = intent.getStringExtra(EXTRA_CONSULTATION_ID) ?: "followup_visit"
        val doctorName = intent.getStringExtra(EXTRA_DOCTOR_NAME) ?: "ডাক্তার"
        val reason = intent.getStringExtra(EXTRA_REASON) ?: "পরবর্তী ফলো-আপ পরামর্শ"

        Timber.d("FollowUpReminderReceiver.onReceive for consultationId=$consultationId, doctor=$doctorName")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mediaware_followup_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = notificationManager.getNotificationChannel(channelId)
            if (existing == null) {
                val channel = NotificationChannel(
                    channelId,
                    "ডাক্তার ফলো-আপ ভিজিট রিমাইন্ডার",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "নির্ধারিত তারিখে ডাক্তারের সাথে পরবর্তী সাক্ষাতের রিমাইন্ডার বার্তা"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            consultationId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val title = "আজ ডাক্তার ফলো-আপ ভিজিটের দিন"
        val message = "$doctorName-এর সাথে আপনার ভিজিট নির্ধারিত। পূর্বের রিপোর্ট, প্রেসক্রিপশন ও ওষুধের তালিকা সাথে রাখুন ($reason)।"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        notificationManager.notify(consultationId.hashCode(), notification)
    }

    companion object {
        const val ACTION_FOLLOW_UP_REMINDER = "com.example.mediaware.ACTION_FOLLOW_UP_REMINDER"
        const val EXTRA_CONSULTATION_ID = "extra_consultation_id"
        const val EXTRA_DOCTOR_NAME = "extra_doctor_name"
        const val EXTRA_REASON = "extra_reason"
    }
}
