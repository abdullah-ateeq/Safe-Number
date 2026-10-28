package com.example.safenumber.data.model

enum class Difficulty(val displayName: String, val initialTimeSeconds: Float, val bonusTimeSeconds: Float, val maxTimeSeconds: Float, val optionCount: Int) {
    EASY(
        displayName = "Easy",
        initialTimeSeconds = 10f,
        bonusTimeSeconds = 1.2f,
        maxTimeSeconds = 12f,
        optionCount = 4
    ),
    MEDIUM(
        displayName = "Medium",
        initialTimeSeconds = 8f,
        bonusTimeSeconds = 1.0f,
        maxTimeSeconds = 10f,
        optionCount = 4
    ),
    HARD(
        displayName = "Hard",
        initialTimeSeconds = 6f,
        bonusTimeSeconds = 0.8f,
        maxTimeSeconds = 8f,
        optionCount = 5
    )
}
