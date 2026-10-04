package com.example.mediaware

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MediAwareApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logging for debug builds
        Timber.plant(Timber.DebugTree())
    }
}
