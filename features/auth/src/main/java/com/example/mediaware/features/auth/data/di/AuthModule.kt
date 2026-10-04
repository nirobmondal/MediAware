package com.example.mediaware.features.auth.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.mediaware.features.auth.data.repository.UserRepositoryImpl
import com.example.mediaware.core.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    companion object {
        @Provides
        @Singleton
        fun provideSessionDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> {
            return context.sessionDataStore
        }
    }
}
