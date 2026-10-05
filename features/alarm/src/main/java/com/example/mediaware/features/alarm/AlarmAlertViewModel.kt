package com.example.mediaware.features.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Screen 31: AlarmAlertActivity.
 *
 * Handles the "dose taken" side-effect — currently logs the action via Timber.
 * The persistence layer (Room) write can be extended here once a ReminderScheduleDao
 * is added to :core:database in a future iteration.
 *
 * Architecture compliance:
 * - @HiltViewModel with injected @IoDispatcher.
 * - No Android context, no GlobalScope.
 * - Domain layer clean: zero android.* imports in business logic.
 */
@HiltViewModel
class AlarmAlertViewModel @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    /**
     * Records that the user has taken the dose for the given [reminderId].
     * Runs on the injected IO dispatcher to keep the UI thread free.
     *
     * NOTE: Extend this to write to a DoseAdherenceDao once the entity is
     * scaffolded in :core:database.
     */
    fun markDoseTaken(reminderId: String) {
        viewModelScope.launch(ioDispatcher) {
            Timber.i("Dose TAKEN recorded: reminderId=$reminderId")
            // TODO: doseAdherenceDao.markTaken(reminderId, System.currentTimeMillis())
        }
    }
}
