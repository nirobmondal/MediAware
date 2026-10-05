package com.example.mediaware.features.auth.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mediaware.features.auth.presentation.login.LoginScreen
import com.example.mediaware.features.auth.presentation.registration.RegistrationScreen
import com.example.mediaware.features.auth.presentation.setup.ProfileSetupScreen
import com.example.mediaware.features.auth.presentation.splash.SplashScreen

object AuthRoutes {
    const val SPLASH = "auth_splash"
    const val REGISTER = "auth_register"
    const val LOGIN = "auth_login"
    const val PROFILE_SETUP = "auth_profile_setup"
}

@Composable
fun AuthNavHost(
    navController: NavHostController = rememberNavController(),
    onAuthenticated: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AuthRoutes.SPLASH
    ) {
        composable(AuthRoutes.SPLASH) {
            SplashScreen(
                onNavigateToRegister = {
                    navController.navigate(AuthRoutes.REGISTER) {
                        popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(AuthRoutes.PROFILE_SETUP) {
                        popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(AuthRoutes.REGISTER) {
            RegistrationScreen(
                onNavigateToProfileSetup = {
                    navController.navigate(AuthRoutes.PROFILE_SETUP) {
                        popUpTo(AuthRoutes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AuthRoutes.LOGIN)
                }
            )
        }

        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onNavigateToHome = onAuthenticated,
                onNavigateToRegister = {
                    navController.navigate(AuthRoutes.REGISTER)
                }
            )
        }

        composable(AuthRoutes.PROFILE_SETUP) {
            ProfileSetupScreen(
                onNavigateToHome = onAuthenticated
            )
        }
    }
}
