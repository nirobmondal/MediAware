package com.example.mediaware.core.domain.repository

import com.example.mediaware.core.model.UpcomingReminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getNextImminentReminderStream(): Flow<UpcomingReminder?>
    suspend fun snoozeReminder(reminderId: String, minutes: Int)
}
