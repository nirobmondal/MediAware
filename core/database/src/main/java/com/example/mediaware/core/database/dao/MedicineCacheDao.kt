package com.example.mediaware.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mediaware.core.database.entity.MedicineCacheEntity

@Dao
interface MedicineCacheDao {

    @Query("SELECT * FROM medicine_cache WHERE generic_name_normalized = :name LIMIT 1")
    suspend fun getCachedMedicine(name: String): MedicineCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(entity: MedicineCacheEntity)

    @Query("UPDATE medicine_cache SET hit_count = hit_count + 1 WHERE generic_name_normalized = :name")
    suspend fun incrementHitCount(name: String)

    @Query("DELETE FROM medicine_cache")
    suspend fun clearAllCache()

    @Query("SELECT * FROM medicine_cache WHERE generic_name_normalized LIKE '%' || :query || '%' OR brand_aliases_json LIKE '%' || :query || '%' OR bangla_name LIKE '%' || :query || '%'")
    suspend fun searchMedicines(query: String): List<MedicineCacheEntity>

    @Query("SELECT COUNT(*) FROM medicine_cache")
    suspend fun getCacheCount(): Int

    @Query("SELECT * FROM medicine_cache ORDER BY hit_count DESC")
    suspend fun getAllCachedMedicines(): List<MedicineCacheEntity>
}
