package com.example.mediaware.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mediaware.features.home.presentation.HomeScreen
import com.example.mediaware.features.settings.presentation.SettingsScreen
import com.example.mediaware.features.auth.navigation.AuthNavHost

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "auth_flow"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("auth_flow") {
            AuthNavHost(
                onAuthenticated = {
                    navController.navigate("home") {
                        popUpTo("auth_flow") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
