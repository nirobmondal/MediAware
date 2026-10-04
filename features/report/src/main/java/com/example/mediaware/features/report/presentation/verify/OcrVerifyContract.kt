package com.example.mediaware.features.report.presentation.verify

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.features.report.domain.model.ExtractedLabItem

data class OcrVerifyUiState(
    val labItems: List<ExtractedLabItem> = emptyList(),
    val editingItem: ExtractedLabItem? = null,
    val showAddDialog: Boolean = false,
    val isLoading: Boolean = false
) : ViewState

sealed interface OcrVerifyUiEvent : ViewEvent {
    data class ParseRawOcr(val rawText: String) : OcrVerifyUiEvent
    data class OnEditItemClicked(val item: ExtractedLabItem) : OcrVerifyUiEvent
    data class OnSaveItemValue(val itemId: String, val newValue: Double) : OcrVerifyUiEvent
    data class OnDeleteItem(val itemId: String) : OcrVerifyUiEvent
    data object OnDismissEditDialog : OcrVerifyUiEvent
    data object OnShowAddDialog : OcrVerifyUiEvent
    data class OnAddNewItem(val testKey: String, val value: Double) : OcrVerifyUiEvent
    data object OnConfirmAndAnalyze : OcrVerifyUiEvent
}

sealed interface OcrVerifySideEffect : ViewSideEffect {
    data class NavigateToAnalysis(val itemsJson: String) : OcrVerifySideEffect
    data class ShowToast(val messageBn: String) : OcrVerifySideEffect
    data object NavigateBack : OcrVerifySideEffect
}
