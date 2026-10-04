package com.example.mediaware.core.database.dao

import androidx.room.*
import com.example.mediaware.core.database.entity.CaregiverLinkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaregiverDao {
    @Query("SELECT * FROM caregiver_link WHERE patient_user_id = :userId")
    fun getCaregiverLinksForPatient(userId: String): Flow<List<CaregiverLinkEntity>>

    @Query("SELECT * FROM caregiver_link WHERE link_id = :linkId")
    suspend fun getCaregiverLinkById(linkId: String): CaregiverLinkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(link: CaregiverLinkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<CaregiverLinkEntity>)

    @Query("UPDATE caregiver_link SET permissions_mask = :mask WHERE link_id = :linkId")
    suspend fun updatePermissions(linkId: String, mask: Int)

    @Query("UPDATE caregiver_link SET link_status = :status WHERE link_id = :linkId")
    suspend fun updateStatus(linkId: String, status: String)

    @Delete
    suspend fun delete(link: CaregiverLinkEntity)

    @Query("DELETE FROM caregiver_link WHERE link_id = :linkId")
    suspend fun deleteById(linkId: String)
}
