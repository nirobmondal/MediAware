package com.example.mediaware.core.database.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TestInfoCacheDao {
    @Query("DELETE FROM test_info_cache")
    suspend fun clearAllCache()
}
