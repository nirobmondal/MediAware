package com.example.mediaware.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mediaware.features.auth.navigation.AuthNavHost
import com.example.mediaware.features.chamber.navigation.ChamberRoutes
import com.example.mediaware.features.chamber.navigation.chamberGraph
import com.example.mediaware.features.consultation.navigation.ConsultationRoutes
import com.example.mediaware.features.consultation.navigation.consultationGraph
import com.example.mediaware.features.history.navigation.HistoryRoutes
import com.example.mediaware.features.history.navigation.historyGraph
import com.example.mediaware.features.home.presentation.HomeScreen
import com.example.mediaware.features.prescription.navigation.RxRoutes
import com.example.mediaware.features.prescription.navigation.prescriptionGraph
import com.example.mediaware.features.report.navigation.ReportRoutes
import com.example.mediaware.features.report.navigation.reportGraph
import com.example.mediaware.features.settings.presentation.SettingsScreen
import com.example.mediaware.features.symptom.navigation.SymptomRoutes
import com.example.mediaware.features.symptom.navigation.symptomGraph

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
                },
                onNavigateToSymptomSelect = {
                    navController.navigate(SymptomRoutes.SELECT)
                },
                onNavigateToReportCapture = {
                    navController.navigate(ReportRoutes.CAPTURE)
                },
                onNavigateToPrescription = {
                    navController.navigate(RxRoutes.CAPTURE)
                },
                onNavigateToChamberHub = {
                    navController.navigate(ChamberRoutes.HUB)
                },
                onNavigateToConsultationSummary = {
                    navController.navigate(ConsultationRoutes.SUMMARY)
                },
                onNavigateToTimeline = {
                    navController.navigate(HistoryRoutes.TIMELINE)
                },
                onNavigateToMedicineHistory = {
                    navController.navigate(HistoryRoutes.MEDICINE_HISTORY)
                },
                onNavigateToReminders = {
                    navController.navigate(HistoryRoutes.REMINDERS)
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("profile") {
            com.example.mediaware.features.auth.presentation.profile.ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        symptomGraph(
            navController = navController,
            onNavigateToHome = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                }
            }
        )
        reportGraph(
            navController = navController,
            onNavigateHome = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                }
            }
        )
        prescriptionGraph(
            navController = navController,
            onNavigateHome = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                }
            }
        )
        chamberGraph(
            navController = navController,
            onNavigateHome = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                }
            },
            onNavigateToSummary = {
                navController.navigate(ConsultationRoutes.SUMMARY)
            }
        )
        consultationGraph(
            navController = navController,
            onNavigateHome = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                }
            },
            onNavigateToRecordAudio = {
                navController.navigate(ChamberRoutes.RECORDER)
            }
        )
        historyGraph(
            navController = navController,
            onNavigateHome = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                }
            }
        )
    }
}
