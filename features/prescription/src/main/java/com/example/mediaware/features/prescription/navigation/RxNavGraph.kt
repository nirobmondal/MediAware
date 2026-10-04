package com.example.mediaware.features.prescription.navigation

import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.mediaware.features.prescription.presentation.RxUiSideEffect
import com.example.mediaware.features.prescription.presentation.RxViewModel
import com.example.mediaware.features.prescription.presentation.capture.RxCaptureScreen
import com.example.mediaware.features.prescription.presentation.explain.MedicineExplainScreen
import com.example.mediaware.features.prescription.presentation.schedule.MedicineScheduleScreen
import com.example.mediaware.features.prescription.presentation.verify.RxVerifyScreen

object RxRoutes {
    const val ROOT = "prescription_root"
    const val CAPTURE = "rx_capture"
    const val VERIFY = "rx_verify"
    const val EXPLAIN = "rx_explain"
    const val SCHEDULE = "rx_schedule"
}

fun NavGraphBuilder.prescriptionGraph(
    navController: NavController,
    onNavigateHome: () -> Unit
) {
    navigation(
        startDestination = RxRoutes.CAPTURE,
        route = RxRoutes.ROOT
    ) {
        composable(RxRoutes.CAPTURE) { backStackEntry ->
            // Use parent entry to share ViewModel across the graph
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RxRoutes.ROOT)
            }
            val viewModel: RxViewModel = hiltViewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                viewModel.sideEffects.collect { effect ->
                    when (effect) {
                        is RxUiSideEffect.NavigateToVerify -> navController.navigate(RxRoutes.VERIFY)
                        is RxUiSideEffect.ShowToast -> Toast.makeText(context, effect.messageBn, Toast.LENGTH_SHORT).show()
                        else -> Unit
                    }
                }
            }

            RxCaptureScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(RxRoutes.VERIFY) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RxRoutes.ROOT)
            }
            val viewModel: RxViewModel = hiltViewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                viewModel.sideEffects.collect { effect ->
                    when (effect) {
                        is RxUiSideEffect.NavigateToExplain -> navController.navigate(RxRoutes.EXPLAIN)
                        is RxUiSideEffect.ShowToast -> Toast.makeText(context, effect.messageBn, Toast.LENGTH_SHORT).show()
                        else -> Unit
                    }
                }
            }

            RxVerifyScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(RxRoutes.EXPLAIN) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RxRoutes.ROOT)
            }
            val viewModel: RxViewModel = hiltViewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsState()

            MedicineExplainScreen(
                uiState = uiState,
                onNavigateToSchedule = { navController.navigate(RxRoutes.SCHEDULE) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(RxRoutes.SCHEDULE) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RxRoutes.ROOT)
            }
            val viewModel: RxViewModel = hiltViewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                viewModel.sideEffects.collect { effect ->
                    when (effect) {
                        is RxUiSideEffect.NavigateToHome -> onNavigateHome()
                        is RxUiSideEffect.ShowToast -> Toast.makeText(context, effect.messageBn, Toast.LENGTH_LONG).show()
                        else -> Unit
                    }
                }
            }

            MedicineScheduleScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
