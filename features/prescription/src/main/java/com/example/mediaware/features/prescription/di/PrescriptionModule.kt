package com.example.mediaware.features.prescription.di

import com.example.mediaware.features.prescription.data.alarm.DoseAlarmSchedulerImpl
import com.example.mediaware.features.prescription.data.repository.MedicineRepositoryImpl
import com.example.mediaware.features.prescription.domain.repository.DoseAlarmScheduler
import com.example.mediaware.features.prescription.domain.repository.MedicineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PrescriptionModule {

    @Binds
    @Singleton
    abstract fun bindMedicineRepository(
        impl: MedicineRepositoryImpl
    ): MedicineRepository

    @Binds
    @Singleton
    abstract fun bindDoseAlarmScheduler(
        impl: DoseAlarmSchedulerImpl
    ): DoseAlarmScheduler
}
