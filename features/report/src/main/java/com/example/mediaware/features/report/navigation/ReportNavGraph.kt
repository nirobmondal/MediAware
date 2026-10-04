package com.example.mediaware.features.report.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mediaware.features.report.presentation.analysis.ReportAnalysisScreen
import com.example.mediaware.features.report.presentation.capture.ReportCaptureScreen
import com.example.mediaware.features.report.presentation.questions.DoctorQuestionsScreen
import com.example.mediaware.features.report.presentation.verify.OcrVerifyScreen

object ReportRoutes {
    const val CAPTURE = "report_capture"
    const val VERIFY = "report_verify/{rawOcrText}"
    const val ANALYSIS = "report_analysis/{encodedItems}"
    const val QUESTIONS = "doctor_questions/{encodedItems}"

    fun createVerifyRoute(rawOcrText: String): String {
        val encoded = Uri.encode(rawOcrText)
        return "report_verify/$encoded"
    }

    fun createAnalysisRoute(encodedItems: String): String {
        val encoded = Uri.encode(encodedItems)
        return "report_analysis/$encoded"
    }

    fun createQuestionsRoute(encodedItems: String): String {
        val encoded = Uri.encode(encodedItems)
        return "doctor_questions/$encoded"
    }
}

fun NavGraphBuilder.reportGraph(
    navController: NavHostController,
    onNavigateHome: () -> Unit
) {
    composable(ReportRoutes.CAPTURE) {
        ReportCaptureScreen(
            onNavigateToVerify = { rawText ->
                navController.navigate(ReportRoutes.createVerifyRoute(rawText))
            },
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = ReportRoutes.VERIFY,
        arguments = listOf(
            navArgument("rawOcrText") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val rawText = Uri.decode(backStackEntry.arguments?.getString("rawOcrText") ?: "")
        OcrVerifyScreen(
            rawOcrText = rawText,
            onNavigateToAnalysis = { itemsData ->
                navController.navigate(ReportRoutes.createAnalysisRoute(itemsData))
            },
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = ReportRoutes.ANALYSIS,
        arguments = listOf(
            navArgument("encodedItems") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val itemsData = Uri.decode(backStackEntry.arguments?.getString("encodedItems") ?: "")
        ReportAnalysisScreen(
            encodedItems = itemsData,
            onNavigateToQuestions = { items ->
                navController.navigate(ReportRoutes.createQuestionsRoute(items))
            },
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = ReportRoutes.QUESTIONS,
        arguments = listOf(
            navArgument("encodedItems") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val itemsData = Uri.decode(backStackEntry.arguments?.getString("encodedItems") ?: "")
        DoctorQuestionsScreen(
            encodedItems = itemsData,
            onNavigateHome = onNavigateHome,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}
