package com.example.mediaware.core.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsultationAudioRecorder @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private var recorder: MediaRecorder? = null
    private var watchdogJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var currentOutputFile: File? = null
    private var recordingStartTimeMs: Long = 0L

    val isRecording: Boolean
        get() = recorder != null

    val currentDurationMs: Long
        get() = if (isRecording) System.currentTimeMillis() - recordingStartTimeMs else 0L

    fun startRecording(
        outputFile: File? = null,
        onWarning14Min: () -> Unit = {},
        onTimeout15Min: () -> Unit = {}
    ): File {
        stopRecording() // Clean up any lingering session

        val targetFile = outputFile ?: createVaultAudioFile()
        currentOutputFile = targetFile

        val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(64_000)
            setAudioSamplingRate(22_050)
            setOutputFile(targetFile.absolutePath)
            prepare()
            start()
        }

        recorder = newRecorder
        recordingStartTimeMs = System.currentTimeMillis()
        Timber.d("Consultation audio recording started: ${targetFile.absolutePath}")

        // 15-Minute Safety Watchdog Cap
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            // 14-Minute Advance Warning (840 seconds)
            delay(14 * 60 * 1000L)
            Timber.w("Audio recorder 14-minute threshold reached; dispatching warning.")
            onWarning14Min()

            // Remaining 1 minute to hard 15-minute cap (900 seconds total)
            delay(1 * 60 * 1000L)
            Timber.w("Audio recorder 15-minute cap expired; automatically stopping hardware recorder.")
            stopRecording()
            onTimeout15Min()
        }

        return targetFile
    }

    fun stopRecording(): File? {
        watchdogJob?.cancel()
        watchdogJob = null

        val file = currentOutputFile
        recorder?.apply {
            try {
                stop()
                release()
                Timber.d("Consultation audio recording successfully saved: ${file?.absolutePath}")
            } catch (e: Exception) {
                Timber.e(e, "Exception while stopping MediaRecorder")
            }
        }
        recorder = null
        recordingStartTimeMs = 0L
        currentOutputFile = null
        return file
    }

    fun getMaxAmplitude(): Int {
        return try {
            recorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun createVaultAudioFile(): File {
        val vaultDir = File(context.filesDir, "vault").apply {
            if (!exists()) mkdirs()
        }
        return File(vaultDir, "consultation_${System.currentTimeMillis()}.m4a")
    }
}
