package com.example.mediaware.core.model

data class UpcomingReminder(
    val reminder_id: String,
    val title_bn: String,
    val target_time: Long,
    val instruction_bn: String,
    val is_fasting_alert: Boolean
)
