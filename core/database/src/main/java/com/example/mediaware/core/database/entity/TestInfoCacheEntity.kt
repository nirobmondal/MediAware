package com.example.mediaware.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_info_cache")
data class TestInfoCacheEntity(
    @PrimaryKey
    val id: String
)
