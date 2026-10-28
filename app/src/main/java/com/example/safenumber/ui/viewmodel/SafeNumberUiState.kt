package com.example.safenumber.ui.viewmodel

import com.example.safenumber.data.model.Difficulty
import com.example.safenumber.data.model.GameStatistics
import com.example.safenumber.data.model.GameState
import com.example.safenumber.data.model.NumberPattern
import com.example.safenumber.data.model.SafeNumberResult

data class SafeNumberUiState(
    val gameState: GameState = GameState.READY,
    val difficulty: Difficulty = Difficulty.EASY,
    val currentPattern: NumberPattern? = null,
    val currentOptions: List<Int> = emptyList(),
    val score: Int = 0,
    val streak: Int = 0,
    val safeSelections: Int = 0,
    val dangerSelections: Int = 0,
    val remainingTime: Float = 10f,
    val maxTime: Float = 12f,
    val countdownNumber: Int = 3,
    val countdownText: String = "3",
    val lastTappedNumber: Int? = null,
    val lastTappedWasSafe: Boolean? = null,
    val feedbackMessage: String? = null,
    val feedbackPoints: Int? = null,
    val milestoneMessage: String? = null,
    val lastResult: SafeNumberResult? = null,
    val statistics: GameStatistics = GameStatistics(),
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themeMode: String = "SYSTEM",
    val showRestartDialog: Boolean = false,
    val showExitDialog: Boolean = false
) {
    val accuracy: Double
        get() {
            val total = safeSelections + dangerSelections
            return if (total == 0) 100.0 else (safeSelections.toDouble() / total) * 100.0
        }

    val isUrgentTime: Boolean
        get() = remainingTime <= 3.0f && gameState == GameState.PLAYING
}
