package com.example.mediaware.features.symptom.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mediaware.features.symptom.presentation.emergency.EmergencyAlertScreen
import com.example.mediaware.features.symptom.presentation.fasting.TestPrepGuideScreen
import com.example.mediaware.features.symptom.presentation.followup.SymptomFollowupScreen
import com.example.mediaware.features.symptom.presentation.prep.VisitPrepScreen
import com.example.mediaware.features.symptom.presentation.select.SymptomSelectScreen

object SymptomRoutes {
    const val SELECT = "symptom_select"
    const val FOLLOWUP = "symptom_followup/{symptomIds}"
    const val EMERGENCY = "emergency_alert/{reasonBn}?matchedSymptoms={matchedSymptoms}"
    const val VISIT_PREP = "visit_prep/{symptomIds}/{severity}/{durationBn}"
    const val TEST_PREP = "test_prep_guide"

    fun createFollowupRoute(symptomIds: List<String>): String {
        val joined = symptomIds.joinToString(",")
        return "symptom_followup/$joined"
    }

    fun createEmergencyRoute(reasonBn: String, matchedSymptoms: List<String>): String {
        val encodedReason = Uri.encode(reasonBn)
        val joinedSymptoms = Uri.encode(matchedSymptoms.joinToString(","))
        return "emergency_alert/$encodedReason?matchedSymptoms=$joinedSymptoms"
    }

    fun createVisitPrepRoute(symptomIds: List<String>, severity: Int, durationBn: String): String {
        val joined = symptomIds.joinToString(",")
        val encodedDuration = Uri.encode(durationBn)
        return "visit_prep/$joined/$severity/$encodedDuration"
    }
}

fun NavGraphBuilder.symptomGraph(
    navController: NavHostController,
    onNavigateToHome: () -> Unit
) {
    composable(SymptomRoutes.SELECT) {
        SymptomSelectScreen(
            onNavigateToFollowup = { ids ->
                navController.navigate(SymptomRoutes.createFollowupRoute(ids))
            },
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = SymptomRoutes.FOLLOWUP,
        arguments = listOf(
            navArgument("symptomIds") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val rawIds = backStackEntry.arguments?.getString("symptomIds") ?: ""
        val ids = rawIds.split(",").filter { it.isNotBlank() }

        SymptomFollowupScreen(
            symptomIds = ids,
            onNavigateToEmergency = { reason, matched ->
                navController.navigate(SymptomRoutes.createEmergencyRoute(reason, matched))
            },
            onNavigateToVisitPrep = { selectedIds, severity, duration ->
                navController.navigate(SymptomRoutes.createVisitPrepRoute(selectedIds, severity, duration))
            },
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = SymptomRoutes.EMERGENCY,
        arguments = listOf(
            navArgument("reasonBn") { type = NavType.StringType },
            navArgument("matchedSymptoms") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val reason = Uri.decode(backStackEntry.arguments?.getString("reasonBn") ?: "")
        val rawMatched = Uri.decode(backStackEntry.arguments?.getString("matchedSymptoms") ?: "")
        val matchedList = rawMatched.split(",").filter { it.isNotBlank() }

        EmergencyAlertScreen(
            reasonBn = reason,
            matchedSymptoms = matchedList,
            onProceedToPrep = {
                // If user chooses to bypass emergency, proceed to general prep or back
                navController.navigate(SymptomRoutes.SELECT) {
                    popUpTo(SymptomRoutes.SELECT) { inclusive = true }
                }
            }
        )
    }

    composable(
        route = SymptomRoutes.VISIT_PREP,
        arguments = listOf(
            navArgument("symptomIds") { type = NavType.StringType },
            navArgument("severity") { type = NavType.IntType; defaultValue = 5 },
            navArgument("durationBn") { type = NavType.StringType; defaultValue = "২-৩ দিন" }
        )
    ) { backStackEntry ->
        val rawIds = backStackEntry.arguments?.getString("symptomIds") ?: ""
        val ids = rawIds.split(",").filter { it.isNotBlank() }
        val severity = backStackEntry.arguments?.getInt("severity") ?: 5
        val duration = Uri.decode(backStackEntry.arguments?.getString("durationBn") ?: "২-৩ দিন")

        VisitPrepScreen(
            symptomIds = ids,
            severity = severity,
            durationBn = duration,
            onNavigateToTestPrep = {
                navController.navigate(SymptomRoutes.TEST_PREP)
            },
            onNavigateToHome = onNavigateToHome,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable(SymptomRoutes.TEST_PREP) {
        TestPrepGuideScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}
