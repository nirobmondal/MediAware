package com.example.mediaware.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediaware.core.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfileEntity)

    @Update
    suspend fun updateUser(user: UserProfileEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Query("SELECT COUNT(*) FROM user_profile")
    suspend fun getUserCount(): Int

    @Query("""
        UPDATE user_profile 
        SET full_name = :fullName, 
            age = :age, 
            gender = :gender, 
            blood_group = :bloodGroup, 
            chronic_conditions_json = :chronicConditions, 
            updated_at = :updatedAt 
        WHERE user_id = :userId
    """)
    suspend fun updateProfileDetails(
        userId: String,
        fullName: String,
        age: Int,
        gender: String,
        bloodGroup: String?,
        chronicConditions: List<String>,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE user_profile SET is_biometric_enabled = :enabled WHERE user_id = :userId")
    suspend fun setBiometricEnabled(userId: String, enabled: Boolean)

    @Query("DELETE FROM user_profile")
    suspend fun clearAll()
}
