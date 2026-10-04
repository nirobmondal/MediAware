package com.example.mediaware.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mediaware.core.database.converter.StringListConverter
import com.example.mediaware.core.database.dao.CaregiverDao
import com.example.mediaware.core.database.dao.MedicineCacheDao
import com.example.mediaware.core.database.dao.TestInfoCacheDao
import com.example.mediaware.core.database.dao.UserProfileDao
import com.example.mediaware.core.database.entity.CaregiverLinkEntity
import com.example.mediaware.core.database.entity.MedicineCacheEntity
import com.example.mediaware.core.database.entity.TestInfoCacheEntity
import com.example.mediaware.core.database.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        MedicineCacheEntity::class,
        TestInfoCacheEntity::class,
        CaregiverLinkEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class MediAwareDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun medicineCacheDao(): MedicineCacheDao
    abstract fun testInfoCacheDao(): TestInfoCacheDao
    abstract fun caregiverDao(): CaregiverDao

    companion object {
        const val DATABASE_NAME = "mediaware_db"
    }
}
