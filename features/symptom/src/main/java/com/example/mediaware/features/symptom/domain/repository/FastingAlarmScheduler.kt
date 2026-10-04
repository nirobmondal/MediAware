package com.example.mediaware.features.symptom.domain.repository

interface FastingAlarmScheduler {
    fun scheduleFastingAlarm(
        testNameBn: String,
        fastingHours: Int,
        triggerEpochMillis: Long,
        reminderMessageBn: String
    ): Boolean

    fun cancelFastingAlarm(testNameBn: String)
}
