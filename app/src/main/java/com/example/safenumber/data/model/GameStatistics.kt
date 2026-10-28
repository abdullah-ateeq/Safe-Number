package com.example.safenumber.data.model

data class GameStatistics(
    val bestScore: Int = 0,
    val bestStreak: Int = 0,
    val bestAccuracy: Double = 0.0,
    val gamesPlayed: Int = 0,
    val totalSafeSelections: Int = 0,
    val totalDangerSelections: Int = 0
)
