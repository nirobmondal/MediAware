package com.example.mediaware.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mediaware.core.database.converter.StringListConverter
import com.example.mediaware.core.database.dao.UserProfileDao
import com.example.mediaware.core.database.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class MediAwareDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        const val DATABASE_NAME = "mediaware_db"
    }
}
