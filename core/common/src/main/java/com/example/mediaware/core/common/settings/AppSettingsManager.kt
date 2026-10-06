package com.example.mediaware.core.common.settings

import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("mediaware_app_settings", Context.MODE_PRIVATE)

    private val _isLargeTextEnabled = MutableStateFlow(prefs.getBoolean(KEY_LARGE_TEXT, false))
    val isLargeTextEnabled: StateFlow<Boolean> = _isLargeTextEnabled.asStateFlow()

    private val _isAudioGuidanceEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUDIO_GUIDANCE, true))
    val isAudioGuidanceEnabled: StateFlow<Boolean> = _isAudioGuidanceEnabled.asStateFlow()

    private val _selectedAvatarId = MutableStateFlow(prefs.getString(KEY_AVATAR_ID, "avatar_teal") ?: "avatar_teal")
    val selectedAvatarId: StateFlow<String> = _selectedAvatarId.asStateFlow()

    private val _customPhotoUri = MutableStateFlow(prefs.getString(KEY_CUSTOM_PHOTO_URI, null))
    val customPhotoUri: StateFlow<String?> = _customPhotoUri.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val resultBd = tts?.setLanguage(Locale("bn", "BD"))
                    if (resultBd == TextToSpeech.LANG_MISSING_DATA || resultBd == TextToSpeech.LANG_NOT_SUPPORTED) {
                        val resultBn = tts?.setLanguage(Locale("bn"))
                        if (resultBn == TextToSpeech.LANG_MISSING_DATA || resultBn == TextToSpeech.LANG_NOT_SUPPORTED) {
                            val resultDef = tts?.setLanguage(Locale.getDefault())
                            if (resultDef == TextToSpeech.LANG_MISSING_DATA || resultDef == TextToSpeech.LANG_NOT_SUPPORTED) {
                                tts?.setLanguage(Locale.US)
                            }
                        }
                    }
                    isTtsInitialized = true
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "TTS initialization failed")
        }
    }

    fun setLargeTextEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LARGE_TEXT, enabled).apply()
        _isLargeTextEnabled.value = enabled
    }

    fun setAudioGuidanceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUDIO_GUIDANCE, enabled).apply()
        _isAudioGuidanceEnabled.value = enabled
        playToggleFeedback(enabled)
    }

    fun setAvatarId(avatarId: String) {
        prefs.edit().putString(KEY_AVATAR_ID, avatarId).apply()
        _selectedAvatarId.value = avatarId
    }

    fun setCustomPhotoUri(uriString: String?) {
        prefs.edit().putString(KEY_CUSTOM_PHOTO_URI, uriString).apply()
        _customPhotoUri.value = uriString
    }

    fun playToggleFeedback(enabled: Boolean) {
        playBeepSound(if (enabled) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_PROP_BEEP2)

        val speechText = if (enabled) {
            "অডিও গাইডেন্স সক্রিয় করা হয়েছে"
        } else {
            "অডিও গাইডেন্স বন্ধ করা হয়েছে"
        }
        speakText(speechText)
    }

    fun playAudioFeedback(textBn: String) {
        if (!_isAudioGuidanceEnabled.value) return

        playBeepSound(ToneGenerator.TONE_PROP_BEEP)
        speakText(textBn)
    }

    private fun playBeepSound(toneType: Int) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(toneType, 220)
        } catch (e: Exception) {
            Timber.e(e, "ToneGenerator error, attempting Ringtone fallback")
            try {
                val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, notificationUri)
                ringtone?.play()
            } catch (re: Exception) {
                Timber.e(re, "Ringtone playback error")
            }
        }
    }

    private fun speakText(text: String) {
        try {
            if (isTtsInitialized && tts != null) {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mediaware_guidance_${System.currentTimeMillis()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "TextToSpeech speak error")
        }
    }

    companion object {
        private const val KEY_LARGE_TEXT = "pref_large_text"
        private const val KEY_AUDIO_GUIDANCE = "pref_audio_guidance"
        private const val KEY_AVATAR_ID = "pref_avatar_id"
        private const val KEY_CUSTOM_PHOTO_URI = "pref_custom_photo_uri"
    }
}
