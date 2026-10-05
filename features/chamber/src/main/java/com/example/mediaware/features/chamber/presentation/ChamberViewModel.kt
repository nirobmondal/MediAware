package com.example.mediaware.features.chamber.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.core.voice.ConsultationAudioRecorder
import com.example.mediaware.features.chamber.domain.model.DoctorQuestionItem
import com.example.mediaware.features.chamber.domain.model.LabSummaryItem
import com.example.mediaware.features.chamber.domain.model.PatientPresentationSummary
import com.example.mediaware.core.database.dao.UserProfileDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ChamberUiState(
    val patientSummary: PatientPresentationSummary = createDefaultPatientSummary(),
    val questions: List<DoctorQuestionItem> = createDefaultQuestions(),
    val hasDoctorConsent: Boolean = false,
    val isRecording: Boolean = false,
    val recordingDurationSeconds: Long = 0L,
    val currentAmplitude: Int = 0,
    val recordedAudioFile: File? = null,
    val is14MinWarningActive: Boolean = false,
    val isTouchLocked: Boolean = false,
    val isRingerMuted: Boolean = false
) {
    val discussedCount: Int
        get() = questions.count { it.isDiscussed }

    val totalQuestionCount: Int
        get() = questions.size

    val progressFormattedBn: String
        get() = "${discussedCount.toString().toBengaliDigits()} / ${totalQuestionCount.toString().toBengaliDigits()}টি সম্পন্ন"

    val durationFormattedBn: String
        get() {
            val mins = recordingDurationSeconds / 60
            val secs = recordingDurationSeconds % 60
            val minStr = (if (mins < 10) "0$mins" else "$mins").toBengaliDigits()
            val secStr = (if (secs < 10) "0$secs" else "$secs").toBengaliDigits()
            return "$minStr:$secStr"
        }
}

sealed interface ChamberUiEvent {
    data class OnToggleDoctorConsent(val hasConsent: Boolean) : ChamberUiEvent
    data object OnStartRecording : ChamberUiEvent
    data object OnStopRecording : ChamberUiEvent
    data class OnToggleQuestion(val id: String) : ChamberUiEvent
    data class OnAddCustomQuestion(val questionBn: String) : ChamberUiEvent
    data class OnToggleTouchLock(val isLocked: Boolean) : ChamberUiEvent
    data object OnToggleRingerMute : ChamberUiEvent
    data object OnDismissWarning : ChamberUiEvent
}

sealed interface ChamberUiSideEffect {
    data class ShowToast(val messageBn: String) : ChamberUiSideEffect
    data object NavigateToQuickRef : ChamberUiSideEffect
    data object NavigateToChecklist : ChamberUiSideEffect
    data object NavigateToRecorder : ChamberUiSideEffect
    data object NavigateBack : ChamberUiSideEffect
}

@HiltViewModel
class ChamberViewModel @Inject constructor(
    private val audioRecorder: ConsultationAudioRecorder,
    private val userProfileDao: UserProfileDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChamberUiState())
    val uiState: StateFlow<ChamberUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<ChamberUiSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private var recordingTickerJob: Job? = null

    init {
        viewModelScope.launch {
            userProfileDao.getUserProfileFlow().collect { profile ->
                if (profile != null) {
                    val chronicText = if (profile.chronicConditions.isNotEmpty()) {
                        profile.chronicConditions.joinToString(", ")
                    } else {
                        "কোনো দীর্ঘমেয়াদী সমস্যা যুক্ত নেই"
                    }
                    val genderText = when (profile.gender.uppercase()) {
                        "MALE" -> "পুরুষ"
                        "FEMALE" -> "মহিলা"
                        else -> profile.gender
                    }

                    _uiState.update { currentState ->
                        currentState.copy(
                            patientSummary = currentState.patientSummary.copy(
                                patientNameBn = profile.fullName.ifBlank { "রোগীর নাম নেই" },
                                ageYears = profile.age,
                                genderBn = genderText,
                                bloodGroup = profile.bloodGroup?.ifBlank { "-" } ?: "-",
                                chronicConditionsBn = chronicText
                            )
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: ChamberUiEvent) {
        when (event) {
            is ChamberUiEvent.OnToggleDoctorConsent -> {
                _uiState.update { it.copy(hasDoctorConsent = event.hasConsent) }
            }

            is ChamberUiEvent.OnStartRecording -> {
                if (!_uiState.value.hasDoctorConsent) {
                    viewModelScope.launch {
                        _sideEffects.send(ChamberUiSideEffect.ShowToast("রেকর্ডিং শুরুর পূর্বে চিকিৎসকের মৌখিক অনুমতি প্রয়োজন।"))
                    }
                    return
                }

                try {
                    val file = audioRecorder.startRecording(
                        onWarning14Min = {
                            _uiState.update { it.copy(is14MinWarningActive = true) }
                        },
                        onTimeout15Min = {
                            stopRecordingInternal(timedOut = true)
                        }
                    )

                    _uiState.update {
                        it.copy(
                            isRecording = true,
                            recordingDurationSeconds = 0L,
                            recordedAudioFile = file,
                            is14MinWarningActive = false
                        )
                    }

                    startTicker()
                } catch (e: Exception) {
                    viewModelScope.launch {
                        _sideEffects.send(ChamberUiSideEffect.ShowToast("মাইক্রোফোন চালু করতে সমস্যা হয়েছে: ${e.message}"))
                    }
                }
            }

            is ChamberUiEvent.OnStopRecording -> {
                stopRecordingInternal(timedOut = false)
            }

            is ChamberUiEvent.OnToggleQuestion -> {
                _uiState.update { state ->
                    state.copy(
                        questions = state.questions.map { q ->
                            if (q.id == event.id) q.copy(isDiscussed = !q.isDiscussed) else q
                        }
                    )
                }
            }

            is ChamberUiEvent.OnAddCustomQuestion -> {
                if (event.questionBn.isNotBlank()) {
                    val newQ = DoctorQuestionItem(
                        questionBn = event.questionBn.trim(),
                        categoryBn = "রোগীর ব্যক্তিগত প্রশ্ন"
                    )
                    _uiState.update { it.copy(questions = it.questions + newQ) }
                }
            }

            is ChamberUiEvent.OnToggleTouchLock -> {
                _uiState.update { it.copy(isTouchLocked = event.isLocked) }
            }

            is ChamberUiEvent.OnToggleRingerMute -> {
                val next = !_uiState.value.isRingerMuted
                _uiState.update { it.copy(isRingerMuted = next) }
                viewModelScope.launch {
                    val msg = if (next) "সাইলেন্ট মোড সক্রিয় হয়েছে।" else "সাধারণ রিংগার মোড সক্রিয় হয়েছে।"
                    _sideEffects.send(ChamberUiSideEffect.ShowToast(msg))
                }
            }

            is ChamberUiEvent.OnDismissWarning -> {
                _uiState.update { it.copy(is14MinWarningActive = false) }
            }
        }
    }

    private fun startTicker() {
        recordingTickerJob?.cancel()
        recordingTickerJob = viewModelScope.launch {
            while (isActive && audioRecorder.isRecording) {
                delay(500)
                val durationSec = audioRecorder.currentDurationMs / 1000
                val amp = audioRecorder.getMaxAmplitude()
                _uiState.update {
                    it.copy(
                        recordingDurationSeconds = durationSec,
                        currentAmplitude = amp
                    )
                }
            }
        }
    }

    private fun stopRecordingInternal(timedOut: Boolean) {
        recordingTickerJob?.cancel()
        recordingTickerJob = null
        val file = audioRecorder.stopRecording()

        _uiState.update {
            it.copy(
                isRecording = false,
                recordedAudioFile = file,
                is14MinWarningActive = false,
                currentAmplitude = 0
            )
        }

        viewModelScope.launch {
            val msg = if (timedOut) {
                "১৫ মিনিটের সীমা উত্তীর্ণ হওয়ায় রেকর্ডিং সফলভাবে সংরক্ষিত হয়েছে।"
            } else {
                "পরামর্শ রেকর্ডিং সফলভাবে ভল্টে সংরক্ষিত হয়েছে।"
            }
            _sideEffects.send(ChamberUiSideEffect.ShowToast(msg))
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (audioRecorder.isRecording) {
            audioRecorder.stopRecording()
        }
    }
}

private fun createDefaultPatientSummary(): PatientPresentationSummary {
    return PatientPresentationSummary(
        patientNameBn = "রোগীর নাম",
        ageYears = 0,
        genderBn = "-",
        bloodGroup = "-",
        chronicConditionsBn = "কোনো দীর্ঘমেয়াদী সমস্যা যোগ করা হয়নি",
        chiefComplaintsBn = "লক্ষণসমূহ ডাক্তারের কাছে তুলে ধরুন",
        complaintDurationBn = "বর্তমান সমস্যা",
        recentLabs = emptyList(),
        trendObservationBn = "",
        currentMedicinesBn = "চলমান কোনো ওষুধ নেই"
    )
}

private fun createDefaultQuestions(): List<DoctorQuestionItem> {
    return listOf(
        DoctorQuestionItem(
            questionBn = "আমার বর্তমান শারীরিক সমস্যা অনুযায়ী কি কোনো বিশেষ সতর্কতা রয়েছে?",
            categoryBn = "সাধারণ স্বাস্থ্য"
        ),
        DoctorQuestionItem(
            questionBn = "বর্তমান প্রেসক্রিপশনের ওষুধগুলো কতদিন সেবন করতে হবে?",
            categoryBn = "ওষুধের নিয়মাবলী"
        ),
        DoctorQuestionItem(
            questionBn = "খাদ্যাভ্যাস বা জীবনযাত্রায় কি কোনো পরিবর্তন আনা প্রয়োজন?",
            categoryBn = "জীবনযাত্রা ও খাদ্যাভ্যাস"
        ),
        DoctorQuestionItem(
            questionBn = "পরবর্তী ফলো-আপ ভিজিটের জন্য কতদিন পর দেখা করা উচিত?",
            categoryBn = "পরবর্তী ফলো-আপ"
        )
    )
}
