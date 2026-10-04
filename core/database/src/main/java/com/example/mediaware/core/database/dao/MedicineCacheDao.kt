package com.example.mediaware.core.database.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MedicineCacheDao {
    @Query("DELETE FROM medicine_cache")
    suspend fun clearAllCache()
}
