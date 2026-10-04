package com.example.mediaware.features.symptom.di

import com.example.mediaware.features.symptom.data.FastingAlarmSchedulerImpl
import com.example.mediaware.features.symptom.domain.repository.FastingAlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SymptomModule {

    @Binds
    @Singleton
    abstract fun bindFastingAlarmScheduler(
        impl: FastingAlarmSchedulerImpl
    ): FastingAlarmScheduler
}
