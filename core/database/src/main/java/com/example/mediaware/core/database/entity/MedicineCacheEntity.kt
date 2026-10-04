package com.example.mediaware.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_cache")
data class MedicineCacheEntity(
    @PrimaryKey
    val id: String
)
