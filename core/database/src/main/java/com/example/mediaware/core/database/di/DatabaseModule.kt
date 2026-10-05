package com.example.mediaware.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.mediaware.core.database.MediAwareDatabase
import com.example.mediaware.core.database.dao.MedicineCacheDao
import com.example.mediaware.core.database.dao.TestInfoCacheDao
import com.example.mediaware.core.database.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MediAwareDatabase {
        // Secure 256-bit passphrase for SQLCipher encrypted Room database
        val passphrase = SQLiteDatabase.getBytes("MediAware@Encrypted#Room2026".toCharArray())
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            MediAwareDatabase::class.java,
            MediAwareDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(database: MediAwareDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideMedicineCacheDao(database: MediAwareDatabase): MedicineCacheDao {
        return database.medicineCacheDao()
    }

    @Provides
    @Singleton
    fun provideTestInfoCacheDao(database: MediAwareDatabase): TestInfoCacheDao {
        return database.testInfoCacheDao()
    }

    @Provides
    @Singleton
    fun provideConsultationDao(database: MediAwareDatabase): com.example.mediaware.core.database.dao.ConsultationDao {
        return database.consultationDao()
    }

    @Provides
    @Singleton
    fun provideHealthRecordDao(database: MediAwareDatabase): com.example.mediaware.core.database.dao.HealthRecordDao {
        return database.healthRecordDao()
    }
}
