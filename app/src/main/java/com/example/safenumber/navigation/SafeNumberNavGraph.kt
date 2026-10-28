package com.example.safenumber.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safenumber.ui.screens.AboutScreen
import com.example.safenumber.ui.screens.GameScreen
import com.example.safenumber.ui.screens.HomeScreen
import com.example.safenumber.ui.screens.ResultScreen
import com.example.safenumber.ui.screens.SettingsScreen
import com.example.safenumber.ui.screens.SplashScreen
import com.example.safenumber.ui.viewmodel.SafeNumberViewModel

@Composable
fun SafeNumberNavGraph(
    viewModel: SafeNumberViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                statistics = uiState.statistics,
                selectedDifficulty = uiState.difficulty,
                onDifficultySelected = { diff -> viewModel.setDifficulty(diff) },
                onPlayClicked = {
                    viewModel.startGame()
                    navController.navigate(Screen.Game.route)
                },
                onSettingsClicked = {
                    navController.navigate(Screen.Settings.route)
                },
                onAboutClicked = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                uiState = uiState,
                onOptionSelected = { num -> viewModel.selectOption(num) },
                onPauseClicked = { viewModel.pauseGame() },
                onResumeClicked = { viewModel.resumeGame() },
                onRestartRequested = { viewModel.showRestartConfirmation(true) },
                onConfirmRestart = { viewModel.restartCurrentGame() },
                onDismissRestart = { viewModel.showRestartConfirmation(false) },
                onExitRequested = { viewModel.showExitConfirmation(true) },
                onConfirmExit = {
                    viewModel.exitGameToHome()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onDismissExit = { viewModel.showExitConfirmation(false) },
                onGameOverTransition = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                result = uiState.lastResult,
                statistics = uiState.statistics,
                onPlayAgainClicked = {
                    viewModel.startGame()
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onHomeClicked = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                themeMode = uiState.themeMode,
                soundEnabled = uiState.soundEnabled,
                hapticsEnabled = uiState.hapticsEnabled,
                onThemeModeChanged = { mode -> viewModel.setThemeMode(mode) },
                onSoundToggled = { enabled -> viewModel.setSoundEnabled(enabled) },
                onHapticsToggled = { enabled -> viewModel.setHapticsEnabled(enabled) },
                onResetStatistics = { viewModel.resetStatistics() },
                onBackClicked = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
