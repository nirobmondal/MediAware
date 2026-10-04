package com.example.mediaware.core.domain.repository

import com.example.mediaware.core.model.SessionStatus
import com.example.mediaware.core.model.UserCredentials
import com.example.mediaware.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun checkSessionStatus(): SessionStatus
    suspend fun registerUser(userId: String, phone: String, pinHash: String, pinSalt: String)
    suspend fun getUserCredentials(): UserCredentials?
    suspend fun updateUserProfile(
        fullName: String,
        age: Int,
        gender: String,
        bloodGroup: String?,
        chronicConditions: List<String>
    )
    fun getUserProfileFlow(): Flow<UserProfile?>
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun clearSession()
}
