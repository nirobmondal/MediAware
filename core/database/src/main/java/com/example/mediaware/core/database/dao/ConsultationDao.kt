package com.example.mediaware.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediaware.core.database.entity.ConsultationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsultationDao {

    @Query("SELECT * FROM consultation_summaries ORDER BY timestamp DESC")
    fun getAllConsultationsFlow(): Flow<List<ConsultationEntity>>

    @Query("SELECT * FROM consultation_summaries WHERE id = :id LIMIT 1")
    suspend fun getConsultationById(id: String): ConsultationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(entity: ConsultationEntity)

    @Update
    suspend fun updateConsultation(entity: ConsultationEntity)

    @Query("DELETE FROM consultation_summaries WHERE id = :id")
    suspend fun deleteConsultationById(id: String)

    @Query("DELETE FROM consultation_summaries")
    suspend fun deleteAllConsultations()
}
