package com.example.mediaware.features.report.presentation.capture

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class ReportCaptureViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseViewModel<ReportCaptureUiState, ReportCaptureUiEvent, ReportCaptureSideEffect>(
    ReportCaptureUiState()
) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onEvent(event: ReportCaptureUiEvent) {
        when (event) {
            ReportCaptureUiEvent.OnToggleFlash -> {
                val nextFlash = !uiState.value.isFlashOn
                setState { copy(isFlashOn = nextFlash) }
            }
            is ReportCaptureUiEvent.OnImageCaptured -> {
                processCapturedImage(event.imageUri)
            }
            is ReportCaptureUiEvent.OnGalleryImageSelected -> {
                processCapturedImage(event.imageUri)
            }
            ReportCaptureUiEvent.OnManualEntryClicked -> {
                sendEffect(ReportCaptureSideEffect.NavigateToVerify(""))
            }
        }
    }

    private fun processCapturedImage(uriString: String?) {
        viewModelScope.launch {
            if (uriString.isNullOrBlank()) {
                sendEffect(ReportCaptureSideEffect.NavigateToVerify(""))
                return@launch
            }

            setState { copy(isProcessingOcr = true, capturedImageUri = uriString) }

            val recognizedText = try {
                val uri = Uri.parse(uriString)
                val image = InputImage.fromFilePath(context, uri)
                suspendCancellableCoroutine<String> { continuation ->
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            if (continuation.isActive) continuation.resume(visionText.text)
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "ML Kit text recognition failed")
                            if (continuation.isActive) continuation.resume("")
                        }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load image for OCR from $uriString")
                ""
            } finally {
                setState { copy(isProcessingOcr = false) }
            }

            sendEffect(ReportCaptureSideEffect.NavigateToVerify(recognizedText))
        }
    }
}
