package com.example.mediaware.features.home.presentation

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.ai.GeminiAiClient
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.common.settings.AppSettingsManager
import com.example.mediaware.core.database.dao.ConsultationDao
import com.example.mediaware.core.database.dao.HealthRecordDao
import com.example.mediaware.core.database.entity.ConsultationEntity
import com.example.mediaware.core.database.entity.HealthRecordEntity
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.core.domain.repository.NetworkMonitor
import com.example.mediaware.core.domain.repository.ReminderRepository
import com.example.mediaware.core.domain.repository.UserRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume

data class ChatState(
    val isChatOpen: Boolean = false,
    val isAiThinking: Boolean = false,
    val isScanningDocument: Boolean = false,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "নমস্কার! আমি MediAware AI ডিজিটাল স্বাস্থ্য সহকারী। আমি পুরোদমে সক্রিয় আছি। আপনার ওষুধ, ল্যাব টেস্ট বা লক্ষণ নিয়ে কথা বলুন অথবা ক্যামেরা দিয়ে ছবি তুলে বিশ্লেষণ করুন।",
            isFromUser = false
        )
    )
)

private data class ChatSettingsTuple(
    val chat: ChatState,
    val avatarId: String,
    val photoUri: String?
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val reminderRepository: ReminderRepository,
    private val consultationDao: ConsultationDao,
    private val healthRecordDao: HealthRecordDao,
    private val geminiAiClient: GeminiAiClient,
    private val appSettingsManager: AppSettingsManager,
    @ApplicationContext private val context: Context,
    networkMonitor: NetworkMonitor
) : BaseViewModel<HomeUiState, HomeUiEvent, HomeSideEffect>(HomeUiState()) {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val banglaDateFormat = SimpleDateFormat("d MMMM yyyy, h:mm a", Locale.forLanguageTag("bn"))
    private val _chatState = MutableStateFlow(ChatState())
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    val combinedState: StateFlow<HomeUiState> = combine(
        combine(
            userRepository.getUserProfileFlow(),
            reminderRepository.getNextImminentReminderStream(),
            networkMonitor.isOnlineStream
        ) { user, nextReminder, isOnline ->
            Triple(user, nextReminder, isOnline)
        },
        consultationDao.getAllConsultationsFlow(),
        healthRecordDao.getRecordsByTypeFlow("SYMPTOM_PREP"),
        healthRecordDao.getRecentRecordsFlow(5),
        combine(
            _chatState,
            appSettingsManager.selectedAvatarId,
            appSettingsManager.customPhotoUri
        ) { chat, avatarId, photoUri ->
            ChatSettingsTuple(chat, avatarId, photoUri)
        }
    ) { userTuple, consultations, prepGuides, healthRecords, chatSettings ->
        val (user, nextReminder, isOnline) = userTuple
        val (chat, avatarId, photoUri) = chatSettings
        HomeUiState(
            userName = user?.fullName ?: "সম্মানিত ব্যবহারকারী",
            userAge = user?.age,
            bloodGroup = user?.bloodGroup,
            chronicConditions = user?.chronicConditions ?: emptyList(),
            isOnline = isOnline,
            selectedAvatarId = avatarId,
            customPhotoUri = photoUri,
            upcomingReminder = nextReminder?.let {
                UpcomingReminderUiModel(
                    reminderId = it.reminder_id,
                    titleBn = it.title_bn,
                    timeFormattedBn = timeFormat.format(Date(it.target_time)).toBengaliDigits(),
                    instructionBn = it.instruction_bn,
                    isFastingAlert = it.is_fasting_alert
                )
            },
            recentConsultation = consultations.firstOrNull(),
            recentConsultations = consultations,
            recentPreparationGuides = prepGuides,
            recentHealthRecords = healthRecords,
            isLoading = false,
            isChatOpen = chat.isChatOpen,
            isAiThinking = chat.isAiThinking,
            isScanningDocument = chat.isScanningDocument,
            chatMessages = chat.chatMessages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    override fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.RefreshData -> { }
            is HomeUiEvent.OnDismissReminder -> {
                viewModelScope.launch {
                    reminderRepository.snoozeReminder(event.reminderId, minutes = 15)
                }
            }
            HomeUiEvent.OnToggleChat -> {
                _chatState.update { it.copy(isChatOpen = !it.isChatOpen) }
            }
            is HomeUiEvent.OnSendChatMessage -> {
                val userMsg = event.message.trim()
                if (userMsg.isBlank()) return

                val newUserMessage = ChatMessage(text = userMsg, isFromUser = true)
                _chatState.update {
                    it.copy(
                        chatMessages = it.chatMessages + newUserMessage,
                        isAiThinking = true
                    )
                }

                viewModelScope.launch {
                    val aiResponse = geminiAiClient.chatWithAi(userMsg)
                    val newAiMessage = ChatMessage(text = aiResponse, isFromUser = false)
                    _chatState.update {
                        it.copy(
                            chatMessages = it.chatMessages + newAiMessage,
                            isAiThinking = false
                        )
                    }
                }
            }
            is HomeUiEvent.OnScanDocumentBitmap -> {
                processImageForDocumentAnalysis(InputImage.fromBitmap(event.bitmap, 0))
            }
            is HomeUiEvent.OnScanDocumentImage -> {
                try {
                    val uri = Uri.parse(event.uriString)
                    val image = InputImage.fromFilePath(context, uri)
                    processImageForDocumentAnalysis(image)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to load input image from uri: ${event.uriString}")
                    _chatState.update {
                        it.copy(
                            isScanningDocument = false,
                            isAiThinking = false,
                            chatMessages = it.chatMessages + ChatMessage(
                                text = "ছবি লোড করতে সমস্যা হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।",
                                isFromUser = false
                            )
                        )
                    }
                }
            }
            is HomeUiEvent.OnUpdateFollowUpDate -> {
                viewModelScope.launch {
                    val entity = consultationDao.getConsultationById(event.consultationId) ?: return@launch
                    val diffDays = ((event.dateMillis - System.currentTimeMillis()) / (24L * 60L * 60L * 1000L)).coerceAtLeast(1L).toInt()
                    val dateBn = SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("bn")).format(Date(event.dateMillis)).toBengaliDigits()
                    val updated = entity.copy(
                        followUpDays = diffDays,
                        followUpDateStringBn = "$dateBn ($diffDays দিন পর)"
                    )
                    consultationDao.updateConsultation(updated)
                    sendEffect(HomeSideEffect.ShowToast("পরবর্তী ভিজিট তারিখ আপডেট হয়েছে: $dateBn"))
                }
            }
            HomeUiEvent.OnClearChat -> {
                _chatState.update {
                    it.copy(
                        chatMessages = listOf(
                            ChatMessage(
                                text = "কথোপকথন রিসেট করা হয়েছে। আপনার নতুন স্বাস্থ্য প্রশ্ন করতে পারেন বা ছবি তুলে নথি স্ক্যান করতে পারেন।",
                                isFromUser = false
                            )
                        )
                    )
                }
            }
        }
    }

    private fun processImageForDocumentAnalysis(inputImage: InputImage) {
        viewModelScope.launch {
            _chatState.update {
                it.copy(
                    isScanningDocument = true,
                    isAiThinking = true,
                    chatMessages = it.chatMessages + ChatMessage(
                        text = "ক্যামেরা দিয়ে মেডিকেল ডকুমেন্ট স্ক্যান করা হয়েছে। লেখা পড়া হচ্ছে এবং WHO ও ডিজিএইচএস গাইডলাইন অনুযায়ী বিশ্লেষণ চলছে...",
                        isFromUser = true
                    )
                )
            }

            val extractedOcrText = try {
                suspendCancellableCoroutine<String> { cont ->
                    textRecognizer.process(inputImage)
                        .addOnSuccessListener { visionText ->
                            if (cont.isActive) cont.resume(visionText.text)
                        }
                        .addOnFailureListener { ex ->
                            Timber.e(ex, "ML Kit OCR failed")
                            if (cont.isActive) cont.resume("")
                        }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during ML Kit processing")
                ""
            }

            if (extractedOcrText.isBlank()) {
                _chatState.update {
                    it.copy(
                        isScanningDocument = false,
                        isAiThinking = false,
                        chatMessages = it.chatMessages + ChatMessage(
                            text = "ছবিটি থেকে কোনো সুস্পষ্ট লেখা পড়া যায়নি। অনুগ্রহ করে পরিষ্কার ও পর্যাপ্ত আলোতে আবার ছবি তুলুন।",
                            isFromUser = false
                        )
                    )
                }
                return@launch
            }

            // Query user's current health context to ground the RAG prompt
            val currentUser = userRepository.getUserProfileFlow().firstOrNull()
            val userContext = currentUser?.let {
                "বয়স: ${it.age} বছর, ক্রনিক রোগ: ${it.chronicConditions.joinToString()}"
            }

            // Perform RAG analysis with WHO + DGHS Guidelines
            val ragResult = try {
                geminiAiClient.analyzeDocumentWithRag(
                    ocrText = extractedOcrText,
                    userContext = userContext
                )
            } catch (e: Exception) {
                Timber.e(e, "RAG analysis failed")
                null
            }

            if (ragResult == null) {
                _chatState.update {
                    it.copy(
                        isScanningDocument = false,
                        isAiThinking = false,
                        chatMessages = it.chatMessages + ChatMessage(
                            text = "ডকুমেন্টটি বিশ্লেষণ করতে সমস্যা হয়েছে। আপনার ইন্টারনেট সংযোগ পরীক্ষা করে পুনরায় চেষ্টা করুন।",
                            isFromUser = false
                        )
                    )
                }
                return@launch
            }

            // Persist the verified analysis into Room SQLite Health Memory
            try {
                val recordEntity = HealthRecordEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    dateFormattedBn = banglaDateFormat.format(Date()).toBengaliDigits(),
                    recordType = ragResult.recordType,
                    title = ragResult.title,
                    summaryBn = ragResult.explanationBn,
                    detailsJson = JSONObject().apply {
                        put("keyPoints", JSONArray(ragResult.keyPoints))
                        put("actionAdvice", ragResult.actionAdviceBn)
                        put("questionsForDoctor", JSONArray(ragResult.questionsForDoctor))
                    }.toString(),
                    sourceGrounding = "WHO & DGHS Guidelines",
                    rawOcrText = extractedOcrText
                )
                healthRecordDao.insertRecord(recordEntity)
            } catch (e: Exception) {
                Timber.e(e, "Failed to persist health record to Room")
            }

            // Format conversational output for chat
            val responseText = buildString {
                appendLine(ragResult.explanationBn)
                appendLine()
                if (ragResult.keyPoints.isNotEmpty()) {
                    appendLine("গুরুত্বপূর্ণ বিষয়সমূহ:")
                    ragResult.keyPoints.forEach { point ->
                        appendLine("• $point")
                    }
                    appendLine()
                }
                if (ragResult.actionAdviceBn.isNotBlank()) {
                    appendLine("করণীয় ও পরামর্শ:")
                    appendLine(ragResult.actionAdviceBn)
                    appendLine()
                }
                if (ragResult.questionsForDoctor.isNotEmpty()) {
                    appendLine("ডাক্তারকে জিজ্ঞেস করার প্রশ্ন:")
                    ragResult.questionsForDoctor.forEach { q ->
                        appendLine("• $q")
                    }
                }
            }.trim()

            _chatState.update {
                it.copy(
                    isScanningDocument = false,
                    isAiThinking = false,
                    chatMessages = it.chatMessages + ChatMessage(
                        text = responseText,
                        isFromUser = false,
                        documentTitle = ragResult.title,
                        savedToMemory = true
                    )
                )
            }
        }
    }
}
