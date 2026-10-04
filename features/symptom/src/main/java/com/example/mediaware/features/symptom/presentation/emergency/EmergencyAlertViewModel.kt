package com.example.mediaware.features.symptom.presentation.emergency

import com.example.mediaware.core.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmergencyAlertViewModel @Inject constructor() :
    BaseViewModel<EmergencyAlertUiState, EmergencyAlertUiEvent, EmergencyAlertSideEffect>(
        EmergencyAlertUiState()
    ) {

    override fun onEvent(event: EmergencyAlertUiEvent) {
        when (event) {
            is EmergencyAlertUiEvent.SetEmergencyReason -> {
                setState {
                    copy(
                        reasonBn = event.reasonBn,
                        matchedSymptomsBn = event.matchedSymptoms
                    )
                }
            }
            EmergencyAlertUiEvent.OnCall999Clicked -> {
                sendEffect(EmergencyAlertSideEffect.TriggerEmergencyCall)
            }
            EmergencyAlertUiEvent.OnAlertCaregiverClicked -> {
                val alertMsg = "জরুরি সতর্কতা (MediAware): আমার তীব্র শারীরিক সমস্যা ও রেড-ফ্ল্যাগ লক্ষণ দেখা দিয়েছে (${uiState.value.reasonBn})। দ্রুত যোগাযোগ করুন।"
                sendEffect(EmergencyAlertSideEffect.SendCaregiverSms(alertMsg))
            }
            EmergencyAlertUiEvent.OnBypassEmergencyClicked -> {
                sendEffect(EmergencyAlertSideEffect.ProceedToVisitPrep)
            }
        }
    }
}
