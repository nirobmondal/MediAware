package com.example.mediaware.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "consultation_summaries",
    indices = [
        Index(value = ["timestamp"], orders = [Index.Order.DESC])
    ]
)
data class ConsultationEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "date_formatted_bn")
    val dateFormattedBn: String,

    @ColumnInfo(name = "doctor_name")
    val doctorName: String,

    @ColumnInfo(name = "summary_bn")
    val summaryBn: String,

    @ColumnInfo(name = "action_items_json")
    val actionItemsJson: String,

    @ColumnInfo(name = "pending_questions_json")
    val pendingQuestionsJson: String,

    @ColumnInfo(name = "follow_up_days")
    val followUpDays: Int,

    @ColumnInfo(name = "follow_up_date_string_bn")
    val followUpDateStringBn: String,

    @ColumnInfo(name = "follow_up_reason_bn")
    val followUpReasonBn: String,

    @ColumnInfo(name = "audio_file_path")
    val audioFilePath: String? = null
)
