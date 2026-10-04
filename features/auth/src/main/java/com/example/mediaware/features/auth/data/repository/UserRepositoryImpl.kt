package com.example.mediaware.features.auth.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mediaware.core.common.di.IoDispatcher
import com.example.mediaware.core.database.dao.UserProfileDao
import com.example.mediaware.core.database.entity.UserProfileEntity
import com.example.mediaware.core.model.Gender
import com.example.mediaware.core.model.SessionStatus
import com.example.mediaware.core.model.UserCredentials
import com.example.mediaware.core.model.UserProfile
import com.example.mediaware.features.auth.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    companion object {
        val KEY_IS_REGISTERED = booleanPreferencesKey("is_registered")
        val KEY_IS_PROFILE_COMPLETE = booleanPreferencesKey("is_profile_complete")
        val KEY_USER_ID = stringPreferencesKey("user_id")
    }

    override suspend fun checkSessionStatus(): SessionStatus = withContext(ioDispatcher) {
        val prefs = dataStore.data.first()
        val dsRegistered = prefs[KEY_IS_REGISTERED] ?: false
        val dsProfileComplete = prefs[KEY_IS_PROFILE_COMPLETE] ?: false

        if (dsRegistered) {
            return@withContext SessionStatus(
                isRegistered = true,
                isProfileComplete = dsProfileComplete
            )
        }

        // Fallback: check Room DB directly
        val user = userProfileDao.getUserProfile()
        if (user != null) {
            val isComplete = user.fullName.isNotBlank() && user.age > 0
            dataStore.edit { p ->
                p[KEY_IS_REGISTERED] = true
                p[KEY_IS_PROFILE_COMPLETE] = isComplete
                p[KEY_USER_ID] = user.userId
            }
            SessionStatus(isRegistered = true, isProfileComplete = isComplete)
        } else {
            SessionStatus(isRegistered = false, isProfileComplete = false)
        }
    }

    override suspend fun registerUser(
        userId: String,
        phone: String,
        pinHash: String,
        pinSalt: String
    ): Unit = withContext(ioDispatcher) {
        val entity = UserProfileEntity(
            userId = userId,
            phoneNumber = phone,
            pinHash = pinHash,
            pinSalt = pinSalt,
            isBiometricEnabled = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        userProfileDao.insertUser(entity)

        dataStore.edit { prefs ->
            prefs[KEY_IS_REGISTERED] = true
            prefs[KEY_IS_PROFILE_COMPLETE] = false
            prefs[KEY_USER_ID] = userId
        }
    }

    override suspend fun getUserCredentials(): UserCredentials? = withContext(ioDispatcher) {
        val user = userProfileDao.getUserProfile() ?: return@withContext null
        UserCredentials(
            userId = user.userId,
            phoneNumber = user.phoneNumber,
            pinHash = user.pinHash,
            pinSalt = user.pinSalt,
            isBiometricEnabled = user.isBiometricEnabled
        )
    }

    override suspend fun updateUserProfile(
        fullName: String,
        age: Int,
        gender: String,
        bloodGroup: String?,
        chronicConditions: List<String>
    ): Unit = withContext(ioDispatcher) {
        val user = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.updateProfileDetails(
            userId = user.userId,
            fullName = fullName,
            age = age,
            gender = gender,
            bloodGroup = bloodGroup,
            chronicConditions = chronicConditions
        )
        dataStore.edit { prefs ->
            prefs[KEY_IS_PROFILE_COMPLETE] = true
        }
    }

    override fun getUserProfileFlow(): Flow<UserProfile?> {
        return userProfileDao.getUserProfileFlow().map { entity ->
            entity?.let {
                val parsedGender = try {
                    Gender.valueOf(it.gender)
                } catch (e: Exception) {
                    Gender.MALE
                }
                UserProfile(
                    userId = it.userId,
                    phoneNumber = it.phoneNumber,
                    fullName = it.fullName,
                    age = it.age,
                    gender = parsedGender,
                    bloodGroup = it.bloodGroup,
                    chronicConditions = it.chronicConditions,
                    isBiometricEnabled = it.isBiometricEnabled,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean): Unit = withContext(ioDispatcher) {
        val user = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.setBiometricEnabled(user.userId, enabled)
    }

    override suspend fun clearSession(): Unit = withContext(ioDispatcher) {
        dataStore.edit { it.clear() }
        userProfileDao.clearAll()
    }
}
