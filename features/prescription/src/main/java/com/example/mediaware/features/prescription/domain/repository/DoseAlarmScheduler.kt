package com.example.mediaware.features.prescription.domain.repository

interface DoseAlarmScheduler {
    fun scheduleExactDailyAlarm(
        reminderId: String,
        hour: Int,
        minute: Int,
        titleBn: String,
        instructionBn: String
    )

    fun cancelAlarm(reminderId: String)
}
