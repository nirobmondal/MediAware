package com.example.mediaware.features.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediaware.features.alarm.receiver.AlarmBroadcastReceiver
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Calendar

/**
 * Screen 31 — Full-Screen Lock Screen Dose Alarm Activity.
 *
 * Launched via a fullScreenIntent PendingIntent from [AlarmBroadcastReceiver].
 * Wakes the device screen even when locked (API 27+ flags set programmatically).
 * Plays a looped system alarm ringtone until the user takes action.
 *
 * Primary actions:
 *  - "✅ খেয়েছি"       → marks dose taken in Room DB and finishes Activity.
 *  - "⏰ ১০ মিনিট পর"  → snoozes alarm by 10 minutes and finishes Activity.
 */
@AndroidEntryPoint
class AlarmAlertActivity : ComponentActivity() {

    private val viewModel: AlarmAlertViewModel by viewModels()
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake the screen and show over lock screen (API 27+ programmatic flags)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val reminderId = intent.getStringExtra(AlarmBroadcastReceiver.EXTRA_REMINDER_ID) ?: ""
        val titleBn = intent.getStringExtra(AlarmBroadcastReceiver.EXTRA_TITLE_BN)
            ?: "ওষুধ খাওয়ার সময় হয়েছে"
        val instructionBn = intent.getStringExtra(AlarmBroadcastReceiver.EXTRA_INSTRUCTION_BN)
            ?: "ডাক্তারের নির্দেশিত ওষুধ সেবন করুন।"

        Timber.d("AlarmAlertActivity started — reminderId=$reminderId, title=$titleBn")

        startAlarmAudio()

        setContent {
            MaterialTheme {
                AlarmAlertScreen(
                    reminderId = reminderId,
                    titleBn = titleBn,
                    instructionBn = instructionBn,
                    onDoseTaken = {
                        viewModel.markDoseTaken(reminderId)
                        stopAlarmAudioAndFinish()
                    },
                    onSnooze = {
                        scheduleSnooze(reminderId, titleBn, instructionBn)
                        stopAlarmAudioAndFinish()
                    }
                )
            }
        }
    }

    /** Plays the default system alarm sound on a loop. */
    private fun startAlarmAudio() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmAlertActivity, alarmUri)
                isLooping = true
                prepare()
                start()
            }
            Timber.d("Alarm audio started")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start alarm audio")
        }
    }

    /** Schedules a +10 minute snooze via AlarmManager. */
    private fun scheduleSnooze(reminderId: String, titleBn: String, instructionBn: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeTimeMs = System.currentTimeMillis() + 10 * 60 * 1000L

        val snoozeIntent = Intent(this, AlarmBroadcastReceiver::class.java).apply {
            action = "com.example.mediaware.ALARM_TRIGGER_FULLSCREEN"
            putExtra(AlarmBroadcastReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(AlarmBroadcastReceiver.EXTRA_TITLE_BN, titleBn)
            putExtra(AlarmBroadcastReceiver.EXTRA_INSTRUCTION_BN, instructionBn)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            (reminderId + "_snooze").hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, snoozeTimeMs, snoozePendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, snoozeTimeMs, snoozePendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, snoozeTimeMs, snoozePendingIntent
                )
            }
            Timber.d("Snooze alarm scheduled for $reminderId in 10 minutes")
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException scheduling snooze for $reminderId")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTimeMs, snoozePendingIntent)
        }
    }

    private fun stopAlarmAudioAndFinish() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

// ─── Composable UI ─────────────────────────────────────────────────────────────

/**
 * Full-screen alarm UI composable for Screen 31.
 *
 * Design: Dark teal background with bold Bengali medicine name, instruction text,
 * and two oversized tactile action buttons for glanceable interaction while groggy.
 */
@Composable
fun AlarmAlertScreen(
    reminderId: String,
    titleBn: String,
    instructionBn: String,
    onDoseTaken: () -> Unit,
    onSnooze: () -> Unit
) {
    val tealDark = Color(0xFF004D40)
    val tealMid = Color(0xFF00695C)
    val amber = Color(0xFFFFC107)
    val white = Color.White

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = tealDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Alarm icon
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = "অ্যালার্ম",
                tint = amber,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App label
            Text(
                text = "MediAware",
                color = amber,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Medicine name — large Bengali heading
            Text(
                text = titleBn,
                color = white,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Instruction text
            if (instructionBn.isNotBlank()) {
                Text(
                    text = instructionBn,
                    color = white.copy(alpha = 0.85f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = tealMid.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Primary CTA — "খেয়েছি" (dose taken)
            Button(
                onClick = onDoseTaken,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),  // green-800
                    contentColor = white
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "খেয়েছি",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary CTA — "১০ মিনিট পর" (snooze 10 minutes)
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = amber
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "১০ মিনিট পর",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Clinical safety disclaimer (Guardrail 3)
            Text(
                text = "এটি কোনো প্রেসক্রিপশন নয়। যেকোনো সিদ্ধান্তে ডাক্তারের পরামর্শ নিন।",
                color = white.copy(alpha = 0.55f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmAlertScreenPreview() {
    MaterialTheme {
        AlarmAlertScreen(
            reminderId = "preview_id",
            titleBn = "মেটফর্মিন ৫০০ মি.গ্রা.",
            instructionBn = "সকালের নাস্তার পর সেবন করুন। দিনে ১ বার।",
            onDoseTaken = {},
            onSnooze = {}
        )
    }
}
