package com.example.mediaware.core.voice.di

import android.content.Context
import com.example.mediaware.core.voice.ConsultationAudioRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VoiceModule {

    @Provides
    @Singleton
    fun provideConsultationAudioRecorder(
        @ApplicationContext context: Context
    ): ConsultationAudioRecorder {
        return ConsultationAudioRecorder(context)
    }
}
