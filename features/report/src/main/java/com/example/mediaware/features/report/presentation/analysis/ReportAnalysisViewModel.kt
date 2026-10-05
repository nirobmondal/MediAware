package com.example.mediaware.features.report.presentation.analysis

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.ai.GeminiAiClient
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.features.report.domain.model.ExtractedLabItem
import com.example.mediaware.features.report.domain.usecase.AnalyzeLabReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReportAnalysisViewModel @Inject constructor(
    private val analyzeLabReportUseCase: AnalyzeLabReportUseCase,
    private val geminiAiClient: GeminiAiClient
) : BaseViewModel<ReportAnalysisUiState, ReportAnalysisUiEvent, ReportAnalysisSideEffect>(
    ReportAnalysisUiState()
) {

    private var currentEncodedData: String = ""

    override fun onEvent(event: ReportAnalysisUiEvent) {
        when (event) {
            is ReportAnalysisUiEvent.LoadAnalysis -> {
                currentEncodedData = event.encodedItems
                parseAndAnalyze(event.encodedItems)
            }
            is ReportAnalysisUiEvent.OnToggleItemTts -> {
                val nextKey = if (uiState.value.currentlyPlayingItemKey == event.itemKey) null else event.itemKey
                setState { copy(currentlyPlayingItemKey = nextKey) }
                if (nextKey != null) {
                    val item = uiState.value.analysis?.items?.firstOrNull { it.key == event.itemKey }
                    sendEffect(ReportAnalysisSideEffect.ShowToast("${item?.testNameBn ?: "টেস্ট"} এর অডিও ব্যাখ্যা শুরু হয়েছে"))
                }
            }
            ReportAnalysisUiEvent.OnToggleFullTts -> {
                val next = !uiState.value.isPlayingTts
                setState { copy(isPlayingTts = next) }
                if (next) {
                    sendEffect(ReportAnalysisSideEffect.ShowToast("সম্পূর্ণ ল্যাব রিপোর্টের বাংলা অডিও ব্যাখ্যা বাজছে"))
                }
            }
            ReportAnalysisUiEvent.OnProceedToQuestions -> {
                sendEffect(ReportAnalysisSideEffect.NavigateToQuestions(currentEncodedData))
            }
        }
    }

    private fun parseAndAnalyze(encoded: String) {
        val items = mutableListOf<ExtractedLabItem>()

        if (encoded.isNotBlank()) {
            val pairs = encoded.split(";")
            for (p in pairs) {
                val parts = p.split(":")
                if (parts.size == 2) {
                    val key = parts[0]
                    val value = parts[1].toDoubleOrNull() ?: continue
                    items.add(createItemFromKeyAndValue(key, value))
                }
            }
        }

        // Fallback default sample if empty
        if (items.isEmpty()) {
            items.addAll(
                listOf(
                    createItemFromKeyAndValue("fbs", 140.0),
                    createItemFromKeyAndValue("creatinine", 1.3),
                    createItemFromKeyAndValue("hemoglobin", 13.5),
                    createItemFromKeyAndValue("cholesterol", 210.0)
                )
            )
        }

        val analysisResult = analyzeLabReportUseCase(items)
        setState { copy(analysis = analysisResult, isLoading = false, isAiAnalyzing = true) }

        // Asynchronously enrich with Gemini AI Explanation in Bengali
        viewModelScope.launch {
            val summaryText = items.joinToString("\n") { 
                "- ${it.testNameBn}: ${it.numericValue} ${it.unit} (স্বাভাবিক মাত্রা: ${it.normalMin}-${it.normalMax} ${it.unit}, স্ট্যাটাস: ${it.status.displayNameBn})"
            }

            // 1. Overall Lab Report AI Analysis
            val overallAi = geminiAiClient.explainLabReportOverall(summaryText)

            // 2. Individual Item AI Explanations
            val enriched = items.map { item ->
                val aiText = geminiAiClient.explainLabTest(
                    testName = item.testNameBn,
                    value = item.numericValue,
                    unit = item.unit,
                    normalMin = item.normalMin,
                    normalMax = item.normalMax,
                    status = item.status.displayNameBn
                )
                item.copy(clinicalExplanationBn = aiText)
            }

            val currentAnalysis = uiState.value.analysis
            if (currentAnalysis != null) {
                setState { 
                    copy(
                        analysis = currentAnalysis.copy(items = enriched),
                        overallAiAnalysisBn = overallAi,
                        isAiAnalyzing = false
                    ) 
                }
            } else {
                setState { 
                    copy(
                        overallAiAnalysisBn = overallAi,
                        isAiAnalyzing = false
                    ) 
                }
            }
        }
    }

    private fun createItemFromKeyAndValue(key: String, value: Double): ExtractedLabItem {
        return when (key) {
            "fbs" -> ExtractedLabItem(UUID.randomUUID().toString(), "fbs", "ফাস্টিং ব্লাড সুগার (FBS)", "Fasting Blood Sugar", value, "mg/dL", 70.0, 99.0, 126.0)
            "rbs" -> ExtractedLabItem(UUID.randomUUID().toString(), "rbs", "র‌্যান্ডম ব্লাড সুগার (RBS)", "Random Blood Sugar", value, "mg/dL", 70.0, 139.0, 200.0)
            "creatinine" -> ExtractedLabItem(UUID.randomUUID().toString(), "creatinine", "সিরাম ক্রিয়েটিনিন (Serum Creatinine)", "Serum Creatinine", value, "mg/dL", 0.6, 1.2, 1.5)
            "hemoglobin" -> ExtractedLabItem(UUID.randomUUID().toString(), "hemoglobin", "হিমোগ্লোবিন (Hemoglobin)", "Hemoglobin", value, "g/dL", 12.0, 16.5, 10.0)
            "hba1c" -> ExtractedLabItem(UUID.randomUUID().toString(), "hba1c", "এইচবিএ১সি (HbA1c)", "HbA1c", value, "%", 4.0, 5.6, 6.5)
            "cholesterol" -> ExtractedLabItem(UUID.randomUUID().toString(), "cholesterol", "টোটাল কোলেস্টেরল (Total Cholesterol)", "Total Cholesterol", value, "mg/dL", 100.0, 199.0, 240.0)
            else -> ExtractedLabItem(UUID.randomUUID().toString(), key, key, key, value, "একক", 0.0, 100.0, 150.0)
        }
    }
}
