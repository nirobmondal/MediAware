package com.example.mediaware.features.report.presentation.verify

import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.features.report.domain.model.ExtractedLabItem
import com.example.mediaware.features.report.domain.usecase.NormalizeOcrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OcrVerifyViewModel @Inject constructor(
    private val normalizeOcrUseCase: NormalizeOcrUseCase
) : BaseViewModel<OcrVerifyUiState, OcrVerifyUiEvent, OcrVerifySideEffect>(OcrVerifyUiState()) {

    override fun onEvent(event: OcrVerifyUiEvent) {
        when (event) {
            is OcrVerifyUiEvent.ParseRawOcr -> {
                val items = normalizeOcrUseCase(event.rawText)
                setState { copy(labItems = items) }
            }
            is OcrVerifyUiEvent.OnEditItemClicked -> {
                setState { copy(editingItem = event.item) }
            }
            is OcrVerifyUiEvent.OnSaveItemValue -> {
                val updated = uiState.value.labItems.map {
                    if (it.id == event.itemId) it.copy(numericValue = event.newValue) else it
                }
                setState { copy(labItems = updated, editingItem = null) }
            }
            is OcrVerifyUiEvent.OnDeleteItem -> {
                val updated = uiState.value.labItems.filterNot { it.id == event.itemId }
                setState { copy(labItems = updated) }
            }
            OcrVerifyUiEvent.OnDismissEditDialog -> {
                setState { copy(editingItem = null, showAddDialog = false) }
            }
            OcrVerifyUiEvent.OnShowAddDialog -> {
                setState { copy(showAddDialog = true) }
            }
            is OcrVerifyUiEvent.OnAddNewItem -> {
                addNewItem(event.testKey, event.value)
            }
            OcrVerifyUiEvent.OnConfirmAndAnalyze -> {
                if (uiState.value.labItems.isEmpty()) {
                    sendEffect(OcrVerifySideEffect.ShowToast("বিশ্লেষণের জন্য কমপক্ষে একটি টেস্টের মান থাকা আবশ্যক"))
                } else {
                    // Encode item keys and values as a compact serialized string for route passing
                    val encodedData = uiState.value.labItems.joinToString(";") {
                        "${it.key}:${it.numericValue}"
                    }
                    sendEffect(OcrVerifySideEffect.NavigateToAnalysis(encodedData))
                }
            }
        }
    }

    private fun addNewItem(testKey: String, value: Double) {
        val newItem = when (testKey) {
            "fbs" -> ExtractedLabItem(UUID.randomUUID().toString(), "fbs", "ফাস্টিং ব্লাড সুগার (FBS)", "Fasting Blood Sugar", value, "mg/dL", 70.0, 99.0, 126.0)
            "rbs" -> ExtractedLabItem(UUID.randomUUID().toString(), "rbs", "র‌্যান্ডম ব্লাড সুগার (RBS)", "Random Blood Sugar", value, "mg/dL", 70.0, 139.0, 200.0)
            "creatinine" -> ExtractedLabItem(UUID.randomUUID().toString(), "creatinine", "সিরাম ক্রিয়েটিনিন (Serum Creatinine)", "Serum Creatinine", value, "mg/dL", 0.6, 1.2, 1.5)
            "hemoglobin" -> ExtractedLabItem(UUID.randomUUID().toString(), "hemoglobin", "হিমোগ্লোবিন (Hemoglobin)", "Hemoglobin", value, "g/dL", 12.0, 16.5, 10.0)
            "hba1c" -> ExtractedLabItem(UUID.randomUUID().toString(), "hba1c", "এইচবিএ১সি (HbA1c)", "HbA1c", value, "%", 4.0, 5.6, 6.5)
            "cholesterol" -> ExtractedLabItem(UUID.randomUUID().toString(), "cholesterol", "টোটাল কোলেস্টেরল (Total Cholesterol)", "Total Cholesterol", value, "mg/dL", 100.0, 199.0, 240.0)
            else -> ExtractedLabItem(UUID.randomUUID().toString(), testKey, testKey, testKey, value, "একক", 0.0, 100.0, 150.0)
        }
        setState { copy(labItems = labItems + newItem, showAddDialog = false) }
    }
}
