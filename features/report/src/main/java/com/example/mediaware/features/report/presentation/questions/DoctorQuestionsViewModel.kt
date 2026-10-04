package com.example.mediaware.features.report.presentation.questions

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.features.report.domain.model.ExtractedLabItem
import com.example.mediaware.features.report.domain.usecase.AnalyzeLabReportUseCase
import com.example.mediaware.features.report.domain.usecase.GenerateLabQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DoctorQuestionsViewModel @Inject constructor(
    private val analyzeLabReportUseCase: AnalyzeLabReportUseCase,
    private val generateLabQuestionsUseCase: GenerateLabQuestionsUseCase
) : BaseViewModel<DoctorQuestionsUiState, DoctorQuestionsUiEvent, DoctorQuestionsSideEffect>(
    DoctorQuestionsUiState()
) {

    override fun onEvent(event: DoctorQuestionsUiEvent) {
        when (event) {
            is DoctorQuestionsUiEvent.LoadQuestions -> {
                loadQuestions(event.encodedItems)
            }
            is DoctorQuestionsUiEvent.OnToggleQuestionChecked -> {
                val updated = uiState.value.questions.map {
                    if (it.id == event.questionId) it.copy(isChecked = !it.isChecked) else it
                }
                setState { copy(questions = updated) }
            }
            DoctorQuestionsUiEvent.OnSaveToConsultationChecklist -> {
                sendEffect(DoctorQuestionsSideEffect.ShowToast("প্রশ্নগুলো চেম্বার চেকলিস্টে সংরক্ষিত হয়েছে"))
            }
            DoctorQuestionsUiEvent.OnNavigateHome -> {
                sendEffect(DoctorQuestionsSideEffect.NavigateHome)
            }
        }
    }

    private fun loadQuestions(encoded: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
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

            val analysis = analyzeLabReportUseCase(items)
            val generatedQuestions = generateLabQuestionsUseCase(analysis.items)

            setState {
                copy(
                    questions = generatedQuestions,
                    isLoading = false,
                    savedCount = generatedQuestions.size
                )
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
