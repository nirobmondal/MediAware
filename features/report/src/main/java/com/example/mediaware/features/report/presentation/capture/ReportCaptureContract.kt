package com.example.mediaware.features.report.presentation.capture

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState

data class ReportCaptureUiState(
    val isFlashOn: Boolean = false,
    val isLowLight: Boolean = false,
    val isProcessingOcr: Boolean = false,
    val capturedImageUri: String? = null
) : ViewState

sealed interface ReportCaptureUiEvent : ViewEvent {
    data object OnToggleFlash : ReportCaptureUiEvent
    data class OnImageCaptured(val imageUri: String?) : ReportCaptureUiEvent
    data class OnGalleryImageSelected(val imageUri: String) : ReportCaptureUiEvent
    data object OnManualEntryClicked : ReportCaptureUiEvent
}

sealed interface ReportCaptureSideEffect : ViewSideEffect {
    data class NavigateToVerify(val rawOcrText: String) : ReportCaptureSideEffect
    data class ShowToast(val messageBn: String) : ReportCaptureSideEffect
    data object NavigateBack : ReportCaptureSideEffect
}
