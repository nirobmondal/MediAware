package com.example.mediaware.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_records",
    indices = [
        Index(value = ["timestamp"], orders = [Index.Order.DESC]),
        Index(value = ["record_type"])
    ]
)
data class HealthRecordEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "date_formatted_bn")
    val dateFormattedBn: String,

    @ColumnInfo(name = "record_type")
    val recordType: String, // "MEDICINE", "LAB_REPORT", "PRESCRIPTION", "SYMPTOM_PREP", "CONSULTATION"

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "summary_bn")
    val summaryBn: String,

    @ColumnInfo(name = "details_json")
    val detailsJson: String? = null,

    @ColumnInfo(name = "source_grounding")
    val sourceGrounding: String = "WHO & DGHS Guidelines",

    @ColumnInfo(name = "raw_ocr_text")
    val rawOcrText: String? = null
)
