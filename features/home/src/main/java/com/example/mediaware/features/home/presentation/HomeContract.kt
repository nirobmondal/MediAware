package com.example.mediaware.features.home.presentation

import android.graphics.Bitmap
import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.core.database.entity.ConsultationEntity
import com.example.mediaware.core.database.entity.HealthRecordEntity

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val documentTitle: String? = null,
    val savedToMemory: Boolean = false
)

data class HomeUiState(
    val userName: String = "",
    val userAge: Int? = null,
    val bloodGroup: String? = null,
    val chronicConditions: List<String> = emptyList(),
    val isOnline: Boolean = true,
    val upcomingReminder: UpcomingReminderUiModel? = null,
    val recentConsultation: ConsultationEntity? = null,
    val recentConsultations: List<ConsultationEntity> = emptyList(),
    val recentPreparationGuides: List<HealthRecordEntity> = emptyList(),
    val recentHealthRecords: List<HealthRecordEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isChatOpen: Boolean = false,
    val isAiThinking: Boolean = false,
    val isScanningDocument: Boolean = false,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "নমস্কার! আমি MediAware AI ডিজিটাল স্বাস্থ্য সহকারী। আমি পুরোদমে সক্রিয় আছি। আপনার ওষুধ, ল্যাব টেস্ট বা লক্ষণ নিয়ে কথা বলুন অথবা ক্যামেরা দিয়ে ছবি তুলে বিশ্লেষণ করুন।",
            isFromUser = false
        )
    )
) : ViewState

data class UpcomingReminderUiModel(
    val reminderId: String,
    val titleBn: String,
    val timeFormattedBn: String,
    val instructionBn: String,
    val isFastingAlert: Boolean
)

sealed interface HomeUiEvent : ViewEvent {
    data object RefreshData : HomeUiEvent
    data class OnDismissReminder(val reminderId: String) : HomeUiEvent
    data object OnToggleChat : HomeUiEvent
    data class OnSendChatMessage(val message: String) : HomeUiEvent
    data class OnScanDocumentImage(val uriString: String) : HomeUiEvent
    data class OnScanDocumentBitmap(val bitmap: Bitmap) : HomeUiEvent
    data object OnClearChat : HomeUiEvent
}

sealed interface HomeSideEffect : ViewSideEffect {
    data class NavigateTo(val route: String) : HomeSideEffect
}
