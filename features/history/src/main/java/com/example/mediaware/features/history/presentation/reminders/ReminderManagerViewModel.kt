package com.example.mediaware.features.history.presentation.reminders

import androidx.lifecycle.ViewModel
import com.example.mediaware.features.history.domain.model.ReminderConflict
import com.example.mediaware.features.history.domain.model.ReminderType
import com.example.mediaware.features.history.domain.model.UnifiedReminder
import com.example.mediaware.features.history.domain.usecase.GetUnifiedRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ReminderManagerUiState(
    val reminders: List<UnifiedReminder> = emptyList(),
    val conflicts: List<ReminderConflict> = emptyList(),
    val selectedFilter: ReminderType? = null,
    val isLoading: Boolean = false
) {
    val filteredReminders: List<UnifiedReminder>
        get() = reminders.filter { selectedFilter == null || it.type == selectedFilter }
}

sealed interface ReminderManagerUiEvent {
    data class OnToggleReminder(val reminderId: String) : ReminderManagerUiEvent
    data class OnSelectFilter(val type: ReminderType?) : ReminderManagerUiEvent
}

@HiltViewModel
class ReminderManagerViewModel @Inject constructor(
    private val getUnifiedRemindersUseCase: GetUnifiedRemindersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderManagerUiState(isLoading = true))
    val uiState: StateFlow<ReminderManagerUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        val result = getUnifiedRemindersUseCase()
        _uiState.update {
            it.copy(
                reminders = result.reminders,
                conflicts = result.conflicts,
                isLoading = false
            )
        }
    }

    fun onEvent(event: ReminderManagerUiEvent) {
        when (event) {
            is ReminderManagerUiEvent.OnToggleReminder -> toggleReminder(event.reminderId)
            is ReminderManagerUiEvent.OnSelectFilter -> {
                _uiState.update { it.copy(selectedFilter = event.type) }
            }
        }
    }

    private fun toggleReminder(reminderId: String) {
        val updated = _uiState.value.reminders.map { rem ->
            if (rem.id == reminderId) rem.copy(isEnabled = !rem.isEnabled) else rem
        }
        val activeReminders = updated.filter { it.isEnabled }
        val conflicts = getUnifiedRemindersUseCase.detectConflicts(activeReminders)

        _uiState.update {
            it.copy(
                reminders = updated,
                conflicts = conflicts
            )
        }
    }
}
