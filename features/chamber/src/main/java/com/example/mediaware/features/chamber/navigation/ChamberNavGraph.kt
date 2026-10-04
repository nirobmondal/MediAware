package com.example.mediaware.features.chamber.navigation

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
import com.example.mediaware.features.chamber.presentation.ChamberUiSideEffect
import com.example.mediaware.features.chamber.presentation.ChamberViewModel
import com.example.mediaware.features.chamber.presentation.checklist.QuestionChecklistScreen
import com.example.mediaware.features.chamber.presentation.hub.ChamberHubScreen
import com.example.mediaware.features.chamber.presentation.quickref.QuickRefCardScreen
import com.example.mediaware.features.chamber.presentation.recorder.ChamberRecorderScreen

object ChamberRoutes {
    const val ROOT = "chamber_root"
    const val HUB = "chamber_hub"
    const val QUICK_REF = "chamber_quick_ref"
    const val CHECKLIST = "chamber_checklist"
    const val RECORDER = "chamber_recorder"
}

fun NavGraphBuilder.chamberGraph(
    navController: NavController,
    onNavigateHome: () -> Unit
) {
    navigation(
        startDestination = ChamberRoutes.HUB,
        route = ChamberRoutes.ROOT
    ) {
        composable(ChamberRoutes.HUB) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(ChamberRoutes.ROOT)
            }
            val viewModel: ChamberViewModel = hiltViewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                viewModel.sideEffects.collect { effect ->
                    when (effect) {
                        is ChamberUiSideEffect.ShowToast -> Toast.makeText(context, effect.messageBn, Toast.LENGTH_SHORT).show()
                        is ChamberUiSideEffect.NavigateToQuickRef -> navController.navigate(ChamberRoutes.QUICK_REF)
                        is ChamberUiSideEffect.NavigateToChecklist -> navController.navigate(ChamberRoutes.CHECKLIST)
                        is ChamberUiSideEffect.NavigateToRecorder -> navController.navigate(ChamberRoutes.RECORDER)
                        is ChamberUiSideEffect.NavigateBack -> navController.popBackStack()
                    }
                }
            }

            ChamberHubScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateToQuickRef = { navController.navigate(ChamberRoutes.QUICK_REF) },
                onNavigateToChecklist = { navController.navigate(ChamberRoutes.CHECKLIST) },
                onNavigateToRecorder = { navController.navigate(ChamberRoutes.RECORDER) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ChamberRoutes.QUICK_REF) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(ChamberRoutes.ROOT)
            }
            val viewModel: ChamberViewModel = hiltViewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsState()

            QuickRefCardScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ChamberRoutes.CHECKLIST) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(ChamberRoutes.ROOT)
            }
            val viewModel: ChamberViewModel = hiltViewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsState()

            QuestionChecklistScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ChamberRoutes.RECORDER) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(ChamberRoutes.ROOT)
            }
            val viewModel: ChamberViewModel = hiltViewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                viewModel.sideEffects.collect { effect ->
                    when (effect) {
                        is ChamberUiSideEffect.ShowToast -> Toast.makeText(context, effect.messageBn, Toast.LENGTH_SHORT).show()
                        else -> Unit
                    }
                }
            }

            ChamberRecorderScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
