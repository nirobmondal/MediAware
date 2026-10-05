package com.example.mediaware.features.consultation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.mediaware.features.consultation.presentation.ConsultationSummaryScreen

object ConsultationRoutes {
    const val ROOT = "consultation_root"
    const val SUMMARY = "consultation_summary"
}

fun NavGraphBuilder.consultationGraph(
    navController: NavController,
    onNavigateHome: () -> Unit,
    onNavigateToRecordAudio: () -> Unit = {}
) {
    navigation(
        startDestination = ConsultationRoutes.SUMMARY,
        route = ConsultationRoutes.ROOT
    ) {
        composable(ConsultationRoutes.SUMMARY) {
            ConsultationSummaryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRecordAudio = onNavigateToRecordAudio
            )
        }
    }
}
