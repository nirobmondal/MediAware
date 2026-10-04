package com.example.mediaware.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "caregiver_link",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["patient_user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["patient_user_id"]),
        Index(value = ["pairing_code_hash"])
    ]
)
data class CaregiverLinkEntity(
    @PrimaryKey
    @ColumnInfo(name = "link_id")
    val linkId: String,

    @ColumnInfo(name = "patient_user_id")
    val patientUserId: String,

    @ColumnInfo(name = "caregiver_user_id")
    val caregiverUserId: String,

    @ColumnInfo(name = "caregiver_name")
    val caregiverName: String,

    @ColumnInfo(name = "pairing_code_hash")
    val pairingCodeHash: String,

    @ColumnInfo(name = "relationship_type")
    val relationshipType: String, // 'DAUGHTER', 'SON', 'SPOUSE', 'OTHER'

    @ColumnInfo(name = "permissions_mask")
    val permissionsMask: Int = 1, // Bit 0 = Read Reports, Bit 1 = View Doses, Bit 2 = Visits/Emergency

    @ColumnInfo(name = "link_status")
    val linkStatus: String = "PENDING", // 'PENDING', 'ACTIVE', 'REVOKED', 'EXPIRED'

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long
)
