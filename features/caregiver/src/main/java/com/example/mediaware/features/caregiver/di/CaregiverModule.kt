package com.example.mediaware.features.caregiver.di

import com.example.mediaware.features.caregiver.data.repository.CaregiverRepositoryImpl
import com.example.mediaware.features.caregiver.domain.repository.CaregiverRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CaregiverModule {

    @Binds
    @Singleton
    abstract fun bindCaregiverRepository(
        impl: CaregiverRepositoryImpl
    ): CaregiverRepository
}
