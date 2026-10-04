package com.example.mediaware.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Gender(val displayNameBn: String) {
    MALE("পুরুষ"),
    FEMALE("মহিলা"),
    OTHER("অন্যান্য")
}

@Serializable
data class UserProfile(
    val userId: String,
    val phoneNumber: String,
    val fullName: String,
    val age: Int,
    val gender: Gender,
    val bloodGroup: String?,
    val chronicConditions: List<String> = emptyList(),
    val isBiometricEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
