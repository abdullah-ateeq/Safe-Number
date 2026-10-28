package com.example.safenumber.data.model

data class SafeNumberResult(
    val difficulty: Difficulty,
    val score: Int,
    val streak: Int,
    val safeSelections: Int,
    val accuracy: Double,
    val reason: GameOverReason,
    val pattern: NumberPattern? = null,
    val tappedNumber: Int? = null,
    val isNewBestScore: Boolean = false,
    val isNewBestStreak: Boolean = false
) {
    val ratingTitle: String
        get() = when {
            streak >= 30 -> "PATTERN MASTER"
            streak >= 20 -> "SAFE NUMBER PRO"
            streak >= 10 -> "PATTERN SPOTTER"
            streak >= 5 -> "GOOD START"
            else -> "KEEP WATCHING"
        }
}
