package com.example.mediaware.features.home.presentation

import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.core.domain.repository.NetworkMonitor
import com.example.mediaware.core.domain.repository.ReminderRepository
import com.example.mediaware.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val reminderRepository: ReminderRepository,
    networkMonitor: NetworkMonitor
) : BaseViewModel<HomeUiState, HomeUiEvent, HomeSideEffect>(HomeUiState()) {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    val combinedState: StateFlow<HomeUiState> = combine(
        userRepository.getUserProfileFlow(),
        reminderRepository.getNextImminentReminderStream(),
        networkMonitor.isOnlineStream
    ) { user, nextReminder, isOnline ->
        HomeUiState(
            userName = user?.fullName ?: "সম্মানিত ব্যবহারকারী",
            isOnline = isOnline,
            upcomingReminder = nextReminder?.let {
                UpcomingReminderUiModel(
                    reminderId = it.reminder_id,
                    titleBn = it.title_bn,
                    timeFormattedBn = timeFormat.format(Date(it.target_time)).toBengaliDigits(),
                    instructionBn = it.instruction_bn,
                    isFastingAlert = it.is_fasting_alert
                )
            },
            isLoading = false
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
        }
    }
}
