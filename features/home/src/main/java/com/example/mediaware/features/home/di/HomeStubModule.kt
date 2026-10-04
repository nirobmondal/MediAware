package com.example.mediaware.features.home.di

import com.example.mediaware.core.domain.repository.NetworkMonitor
import com.example.mediaware.core.domain.repository.ReminderRepository
import com.example.mediaware.core.model.UpcomingReminder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeStubModule {
    @Provides
    @Singleton
    fun provideNetworkMonitor(): NetworkMonitor = object : NetworkMonitor {
        override val isOnlineStream: Flow<Boolean> = flowOf(true)
    }

    @Provides
    @Singleton
    fun provideReminderRepository(): ReminderRepository = object : ReminderRepository {
        override fun getNextImminentReminderStream(): Flow<UpcomingReminder?> = flowOf(null)
        override suspend fun snoozeReminder(reminderId: String, minutes: Int) {}
    }
}
