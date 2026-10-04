package com.example.mediaware.features.report.presentation.capture

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportCaptureViewModel @Inject constructor() :
    BaseViewModel<ReportCaptureUiState, ReportCaptureUiEvent, ReportCaptureSideEffect>(
        ReportCaptureUiState()
    ) {

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

    private fun processCapturedImage(uri: String?) {
        viewModelScope.launch {
            setState { copy(isProcessingOcr = true, capturedImageUri = uri) }
            // Simulating image pre-processing & ML Kit text recognition
            delay(600)
            setState { copy(isProcessingOcr = false) }

            // Sample raw OCR text mimicking standard printed lab slips in Bangladesh
            val sampleOcrText = """
                LABORATORY INVESTIGATION REPORT
                Patient Name: Karim Mia, Age: 50
                ------------------------------------------------
                TEST NAME              RESULT       UNIT    REFERENCE
                ------------------------------------------------
                Fasting Blood Sugar    140.0        mg/dL   70.0 - 99.0
                Serum Creatinine       1.3          mg/dL   0.6 - 1.2
                Hemoglobin             13.5         g/dL    12.0 - 16.5
                Total Cholesterol      210.0        mg/dL   100 - 199
                ------------------------------------------------
            """.trimIndent()

            sendEffect(ReportCaptureSideEffect.NavigateToVerify(sampleOcrText))
        }
    }
}
