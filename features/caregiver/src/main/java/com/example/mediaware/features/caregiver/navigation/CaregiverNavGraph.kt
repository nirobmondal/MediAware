package com.example.mediaware.features.caregiver.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.mediaware.features.caregiver.presentation.add.AddCaregiverScreen
import com.example.mediaware.features.caregiver.presentation.manage.CaregiverManageScreen
import com.example.mediaware.features.caregiver.presentation.profile.LinkedProfileScreen

object CaregiverRoutes {
    const val ADD = "caregiver_add"
    const val MANAGE = "caregiver_manage"
    const val PROFILE = "caregiver_profile"
}

fun NavGraphBuilder.caregiverGraph(
    navController: NavHostController,
    onNavigateHome: () -> Unit
) {
    composable(CaregiverRoutes.ADD) {
        AddCaregiverScreen(
            onNavigateBack = {
                if (!navController.popBackStack()) {
                    onNavigateHome()
                }
            },
            onNavigateToManage = {
                navController.navigate(CaregiverRoutes.MANAGE)
            }
        )
    }

    composable(CaregiverRoutes.MANAGE) {
        CaregiverManageScreen(
            onNavigateBack = {
                if (!navController.popBackStack()) {
                    onNavigateHome()
                }
            },
            onNavigateToAdd = {
                navController.navigate(CaregiverRoutes.ADD)
            },
            onNavigateToProfile = {
                navController.navigate(CaregiverRoutes.PROFILE)
            }
        )
    }

    composable(CaregiverRoutes.PROFILE) {
        LinkedProfileScreen(
            onNavigateBack = {
                if (!navController.popBackStack()) {
                    onNavigateHome()
                }
            }
        )
    }
}
