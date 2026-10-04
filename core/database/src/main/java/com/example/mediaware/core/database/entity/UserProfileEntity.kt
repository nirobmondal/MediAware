package com.example.mediaware.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_profile",
    indices = [
        Index(value = ["phone_number"], unique = true)
    ]
)
data class UserProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "full_name")
    val fullName: String = "",

    @ColumnInfo(name = "age")
    val age: Int = 0,

    @ColumnInfo(name = "gender")
    val gender: String = "MALE",

    @ColumnInfo(name = "blood_group")
    val bloodGroup: String? = null,

    @ColumnInfo(name = "chronic_conditions_json")
    val chronicConditions: List<String> = emptyList(),

    @ColumnInfo(name = "pin_hash")
    val pinHash: String,

    @ColumnInfo(name = "pin_salt")
    val pinSalt: String,

    @ColumnInfo(name = "is_biometric_enabled")
    val isBiometricEnabled: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
