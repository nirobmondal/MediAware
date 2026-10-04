package com.example.mediaware.features.history.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.mediaware.features.history.presentation.adherence.MedicineHistoryScreen
import com.example.mediaware.features.history.presentation.reminders.ReminderManagerScreen
import com.example.mediaware.features.history.presentation.timeline.HealthTimelineScreen

object HistoryRoutes {
    const val ROOT = "history_root"
    const val TIMELINE = "history_timeline"
    const val MEDICINE_HISTORY = "history_medicine"
    const val REMINDERS = "history_reminders"
}

fun NavGraphBuilder.historyGraph(
    navController: NavController,
    onNavigateHome: () -> Unit
) {
    navigation(
        startDestination = HistoryRoutes.TIMELINE,
        route = HistoryRoutes.ROOT
    ) {
        composable(HistoryRoutes.TIMELINE) {
            HealthTimelineScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(HistoryRoutes.MEDICINE_HISTORY) {
            MedicineHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(HistoryRoutes.REMINDERS) {
            ReminderManagerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
