package com.example.mediaware.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicine_cache",
    indices = [Index(value = ["generic_name_normalized"], unique = true)]
)
data class MedicineCacheEntity(
    @PrimaryKey
    val generic_name_normalized: String,
    val brand_aliases_json: String = "[]",
    val bangla_name: String,
    val therapeutic_class: String,
    val primary_purpose_bn: String,
    val standard_dosage_bn: String,
    val common_side_effects_bn: String? = null,
    val critical_warnings_bn: String,
    val lifestyle_precautions_bn: String? = null,
    val cached_at: Long,
    val ttl_timestamp: Long,
    val hit_count: Int = 1
)
