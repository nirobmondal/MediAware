package com.example.mediaware.core.model

data class SessionStatus(
    val isRegistered: Boolean,
    val isProfileComplete: Boolean
)

data class UserCredentials(
    val userId: String,
    val phoneNumber: String,
    val pinHash: String,
    val pinSalt: String,
    val isBiometricEnabled: Boolean
)
