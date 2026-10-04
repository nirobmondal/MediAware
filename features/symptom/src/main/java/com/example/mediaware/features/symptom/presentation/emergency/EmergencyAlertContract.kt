package com.example.mediaware.features.symptom.presentation.emergency

import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState

data class EmergencyAlertUiState(
    val reasonBn: String = "বুকব্যথা এবং শ্বাসকষ্ট একসাথে হৃদরোগ বা হার্ট অ্যাটাকের মারাত্মক লক্ষণ হতে পারে।",
    val matchedSymptomsBn: List<String> = emptyList(),
    val isPlayingWarningTone: Boolean = true
) : ViewState

sealed interface EmergencyAlertUiEvent : ViewEvent {
    data class SetEmergencyReason(val reasonBn: String, val matchedSymptoms: List<String>) : EmergencyAlertUiEvent
    data object OnCall999Clicked : EmergencyAlertUiEvent
    data object OnAlertCaregiverClicked : EmergencyAlertUiEvent
    data object OnBypassEmergencyClicked : EmergencyAlertUiEvent
}

sealed interface EmergencyAlertSideEffect : ViewSideEffect {
    data object TriggerEmergencyCall : EmergencyAlertSideEffect
    data class SendCaregiverSms(val alertText: String) : EmergencyAlertSideEffect
    data object ProceedToVisitPrep : EmergencyAlertSideEffect
}
