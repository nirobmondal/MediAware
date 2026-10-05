package com.example.mediaware.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediaware.core.database.entity.HealthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {

    @Query("SELECT * FROM health_records ORDER BY timestamp DESC")
    fun getAllRecordsFlow(): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE record_type = :type ORDER BY timestamp DESC")
    fun getRecordsByTypeFlow(type: String): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: String): HealthRecordEntity?

    @Query("SELECT * FROM health_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRecordsFlow(limit: Int): Flow<List<HealthRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(entity: HealthRecordEntity)

    @Update
    suspend fun updateRecord(entity: HealthRecordEntity)

    @Query("DELETE FROM health_records WHERE id = :id")
    suspend fun deleteRecordById(id: String)

    @Query("DELETE FROM health_records")
    suspend fun deleteAllRecords()
}
