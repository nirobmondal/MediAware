package com.example.mediaware.features.symptom.presentation.prep

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.ai.GeminiAiClient
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.database.dao.ConsultationDao
import com.example.mediaware.core.database.dao.HealthRecordDao
import com.example.mediaware.core.database.entity.HealthRecordEntity
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.core.domain.repository.UserRepository
import com.example.mediaware.features.symptom.domain.model.Symptom
import com.example.mediaware.features.symptom.domain.repository.SymptomCatalog
import com.example.mediaware.features.symptom.domain.usecase.GenerateVisitPrepUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VisitPrepViewModel @Inject constructor(
    private val generateVisitPrepUseCase: GenerateVisitPrepUseCase,
    private val userRepository: UserRepository,
    private val consultationDao: ConsultationDao,
    private val healthRecordDao: HealthRecordDao,
    private val geminiAiClient: GeminiAiClient
) : BaseViewModel<VisitPrepUiState, VisitPrepUiEvent, VisitPrepSideEffect>(VisitPrepUiState()) {

    private val banglaDateFormat = SimpleDateFormat("d MMMM yyyy, h:mm a", Locale.forLanguageTag("bn"))

    private var lastSymptomIds: List<String> = emptyList()
    private var lastSeverity: Int = 5
    private var lastDurationBn: String = "কয়েক দিন"

    override fun onEvent(event: VisitPrepUiEvent) {
        when (event) {
            is VisitPrepUiEvent.GenerateCard -> {
                lastSymptomIds = event.symptomIds
                lastSeverity = event.severity
                lastDurationBn = event.durationBn
                generateCard(event.symptomIds, event.severity, event.durationBn)
            }
            VisitPrepUiEvent.OnRetry -> {
                generateCard(lastSymptomIds, lastSeverity, lastDurationBn)
            }
            VisitPrepUiEvent.OnToggleTts -> {
                val next = !uiState.value.isPlayingTts
                setState { copy(isPlayingTts = next) }
                if (next) {
                    sendEffect(VisitPrepSideEffect.ShowToast("অডিও চালু হয়েছে: ডাক্তারের কাছে বলার পয়েন্টসমূহ"))
                }
            }
            VisitPrepUiEvent.OnFinishAndGoHome -> {
                sendEffect(VisitPrepSideEffect.NavigateToHome)
            }
        }
    }

    private fun generateCard(symptomIds: List<String>, severity: Int, durationBn: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, isNetworkError = false, isAiAnalyzing = true) }
            val profile = userRepository.getUserProfileFlow().firstOrNull()
            val catalog = SymptomCatalog.ALL_SYMPTOMS.associateBy { it.id }
            val resolvedSymptoms = symptomIds.map { id ->
                catalog[id] ?: Symptom(
                    id = id,
                    nameBn = id.removePrefix("custom_"),
                    anatomicalRegionBn = "লক্ষণ",
                    isRedFlagPotential = false,
                    iconName = "healing"
                )
            }

            // Retrieve Health Memory context to ground the LLM consultation plan
            val pastConsultations = consultationDao.getAllConsultationsFlow().firstOrNull()
            val pastHealthRecords = healthRecordDao.getRecentRecordsFlow(5).firstOrNull()

            val memoryContext = buildString {
                pastConsultations?.firstOrNull()?.let {
                    append("সর্বশেষ ডাক্তার পরামর্শ: ${it.doctorName} - ${it.summaryBn}. ")
                }
                pastHealthRecords?.take(3)?.forEach { rec ->
                    append("${rec.title}: ${rec.summaryBn.take(80)}. ")
                }
            }.trim()

            val userSummary = profile?.let {
                "বয়স: ${it.age} বছর, লিঙ্গ: ${it.gender.displayNameBn}, দীর্ঘস্থায়ী রোগ: ${it.chronicConditions.joinToString()}"
            }

            // Asynchronously generate 100% dynamic AI Symptom Plan & Doctor Cheat Questions
            val planResult = geminiAiClient.planSymptomConsultation(
                symptoms = resolvedSymptoms.map { it.nameBn },
                severity = severity,
                duration = durationBn,
                accompanyingSymptoms = emptyList(),
                healthMemoryContext = if (memoryContext.isNotBlank()) memoryContext else null,
                userProfileSummary = userSummary
            )

            if (planResult != null) {
                val dynamicCard = generateVisitPrepUseCase(
                    userProfile = profile,
                    symptoms = resolvedSymptoms,
                    severityRating = severity,
                    durationBn = durationBn
                ).copy(
                    doctorSpeakingPointsBn = planResult.speakingPoints,
                    doctorQuestionsBn = planResult.cheatQuestionsForDoctor,
                    suggestedSpecialistBn = planResult.suggestedSpecialistBn
                )

                // Automatically persist this 100% dynamic visit prep into Room SQLite Health Memory
                try {
                    val prepRecord = HealthRecordEntity(
                        id = UUID.randomUUID().toString(),
                        timestamp = System.currentTimeMillis(),
                        dateFormattedBn = banglaDateFormat.format(Date()).toBengaliDigits(),
                        recordType = "SYMPTOM_PREP",
                        title = "ভিজিট প্রস্তুতি: ${resolvedSymptoms.joinToString { it.nameBn }}",
                        summaryBn = planResult.triageAssessmentBn,
                        detailsJson = JSONObject().apply {
                            put("speakingPoints", JSONArray(planResult.speakingPoints))
                            put("whatToShowDoctor", JSONArray(planResult.whatToShowDoctor))
                            put("cheatQuestions", JSONArray(planResult.cheatQuestionsForDoctor))
                            put("homeCareAdvice", planResult.homeCareAdviceBn)
                            put("suggestedSpecialist", planResult.suggestedSpecialistBn)
                            put("symptoms", JSONArray(resolvedSymptoms.map { it.nameBn }))
                        }.toString(),
                        sourceGrounding = "DGHS & WHO Triage Protocol"
                    )
                    healthRecordDao.insertRecord(prepRecord)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to persist symptom prep to health memory")
                }

                setState {
                    copy(
                        visitCard = dynamicCard,
                        aiSymptomAnalysisBn = planResult.triageAssessmentBn,
                        aiCheatQuestions = planResult.cheatQuestionsForDoctor,
                        whatToShowDoctor = planResult.whatToShowDoctor,
                        homeCareAdviceBn = planResult.homeCareAdviceBn,
                        needsDoctorVisit = planResult.needsDoctorVisit,
                        isSavedToHealthMemory = true,
                        isLoading = false,
                        isNetworkError = false,
                        isAiAnalyzing = false
                    )
                }
            } else {
                // If offline / network error, inform UI to show retry state rather than fabricated templates
                setState {
                    copy(
                        isLoading = false,
                        isNetworkError = true,
                        isAiAnalyzing = false
                    )
                }
            }
        }
    }
}
