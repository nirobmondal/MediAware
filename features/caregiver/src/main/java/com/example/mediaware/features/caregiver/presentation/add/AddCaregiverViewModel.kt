package com.example.mediaware.features.caregiver.presentation.add

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.common.base.ViewEvent
import com.example.mediaware.core.common.base.ViewSideEffect
import com.example.mediaware.core.common.base.ViewState
import com.example.mediaware.core.common.di.DefaultDispatcher
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.caregiver.domain.model.PairingCode
import com.example.mediaware.features.caregiver.domain.usecase.GeneratePairingCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddCaregiverUiState(
    val pairingCode: PairingCode? = null,
    val remainingSeconds: Long = 600L,
    val totalSeconds: Long = 600L,
    val formattedTimeBn: String = "১০:০০",
    val progress: Float = 1.0f,
    val isExpired: Boolean = false,
    val isLoading: Boolean = false
) : ViewState

sealed interface AddCaregiverUiEvent : ViewEvent {
    data object GenerateNewCode : AddCaregiverUiEvent
    data object CopyCode : AddCaregiverUiEvent
    data object ShareLink : AddCaregiverUiEvent
}

sealed interface AddCaregiverSideEffect : ViewSideEffect {
    data class ShowSnackbar(val message: String) : AddCaregiverSideEffect
    data class ShareDeepLink(val uri: String, val message: String) : AddCaregiverSideEffect
}

@HiltViewModel
class AddCaregiverViewModel @Inject constructor(
    private val generatePairingCodeUseCase: GeneratePairingCodeUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : BaseViewModel<AddCaregiverUiState, AddCaregiverUiEvent, AddCaregiverSideEffect>(
    AddCaregiverUiState()
) {

    private var countdownJob: Job? = null

    init {
        generateCode()
    }

    override fun onEvent(event: AddCaregiverUiEvent) {
        when (event) {
            is AddCaregiverUiEvent.GenerateNewCode -> generateCode()
            is AddCaregiverUiEvent.CopyCode -> {
                uiState.value.pairingCode?.let { code ->
                    sendEffect(AddCaregiverSideEffect.ShowSnackbar("পেয়ারিং কোড ${code.formattedCodeBn} কপি করা হয়েছে"))
                }
            }
            is AddCaregiverUiEvent.ShareLink -> {
                uiState.value.pairingCode?.let { code ->
                    val shareMsg = "MediAware কেয়ারগিভার পেয়ারিং কোড: ${code.formattedCodeBn}\nএই কোডের মেয়াদ ১০ মিনিট।\nঅ্যাপ লিংক: ${code.deepLinkUri}"
                    sendEffect(AddCaregiverSideEffect.ShareDeepLink(code.deepLinkUri, shareMsg))
                }
            }
        }
    }

    private fun generateCode() {
        countdownJob?.cancel()
        val newCode = generatePairingCodeUseCase(ttlSeconds = 600L)
        val initialRemaining = 600L
        setState {
            copy(
                pairingCode = newCode,
                remainingSeconds = initialRemaining,
                totalSeconds = 600L,
                formattedTimeBn = formatSecondsToMinutesBn(initialRemaining),
                progress = 1.0f,
                isExpired = false,
                isLoading = false
            )
        }
        startCountdown(initialRemaining)
    }

    private fun startCountdown(durationSeconds: Long) {
        countdownJob = viewModelScope.launch(defaultDispatcher) {
            var current = durationSeconds
            while (isActive && current > 0) {
                delay(1000L)
                current -= 1
                val remaining = current
                val prog = (remaining.toFloat() / durationSeconds.toFloat()).coerceIn(0f, 1f)
                val timeBn = formatSecondsToMinutesBn(remaining)
                val expired = remaining <= 0

                setState {
                    copy(
                        remainingSeconds = remaining,
                        formattedTimeBn = timeBn,
                        progress = prog,
                        isExpired = expired
                    )
                }
            }
        }
    }

    private fun formatSecondsToMinutesBn(totalSec: Long): String {
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        val minStr = minutes.toString().padStart(2, '0').toBengaliDigits()
        val secStr = seconds.toString().padStart(2, '0').toBengaliDigits()
        return "$minStr:$secStr"
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
