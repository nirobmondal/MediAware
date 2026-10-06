package com.example.mediaware

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.fragment.app.FragmentActivity
import com.example.mediaware.core.common.settings.AppSettingsManager
import com.example.mediaware.core.designsystem.theme.MediAwareTheme
import com.example.mediaware.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var appSettingsManager: AppSettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isLargeText by appSettingsManager.isLargeTextEnabled.collectAsState()
            val currentDensity = LocalDensity.current
            val customDensity = remember(currentDensity, isLargeText) {
                Density(
                    density = currentDensity.density,
                    fontScale = if (isLargeText) currentDensity.fontScale * 1.25f else currentDensity.fontScale
                )
            }

            CompositionLocalProvider(LocalDensity provides customDensity) {
                MediAwareTheme {
                    AppNavHost()
                }
            }
        }
    }
}