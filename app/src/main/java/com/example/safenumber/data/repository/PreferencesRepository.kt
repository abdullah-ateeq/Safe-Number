package com.example.safenumber.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.safenumber.data.model.Difficulty
import com.example.safenumber.data.model.GameStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _statistics = MutableStateFlow(loadStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_ENABLED, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTICS_ENABLED, true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _difficulty = MutableStateFlow(
        try {
            Difficulty.valueOf(prefs.getString(KEY_DIFFICULTY, Difficulty.EASY.name) ?: Difficulty.EASY.name)
        } catch (_: Exception) {
            Difficulty.EASY
        }
    )
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()

    private fun loadStatistics(): GameStatistics {
        return GameStatistics(
            bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
            bestStreak = prefs.getInt(KEY_BEST_STREAK, 0),
            bestAccuracy = prefs.getFloat(KEY_BEST_ACCURACY, 0f).toDouble(),
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            totalSafeSelections = prefs.getInt(KEY_TOTAL_SAFE, 0),
            totalDangerSelections = prefs.getInt(KEY_TOTAL_DANGER, 0)
        )
    }

    /**
     * Records a game's outcome and updates records.
     * Returns Pair(isNewBestScore, isNewBestStreak).
     */
    fun recordGameOutcome(
        score: Int,
        streak: Int,
        gameSafeCount: Int,
        gameDangerCount: Int,
        gameAccuracy: Double
    ): Pair<Boolean, Boolean> {
        val currentStats = _statistics.value
        val isNewBestScore = score > currentStats.bestScore && score > 0
        val isNewBestStreak = streak > currentStats.bestStreak && streak > 0

        val newBestScore = max(currentStats.bestScore, score)
        val newBestStreak = max(currentStats.bestStreak, streak)
        val newBestAccuracy = max(currentStats.bestAccuracy, gameAccuracy)
        val newGamesPlayed = currentStats.gamesPlayed + 1
        val newTotalSafe = currentStats.totalSafeSelections + gameSafeCount
        val newTotalDanger = currentStats.totalDangerSelections + gameDangerCount

        prefs.edit()
            .putInt(KEY_BEST_SCORE, newBestScore)
            .putInt(KEY_BEST_STREAK, newBestStreak)
            .putFloat(KEY_BEST_ACCURACY, newBestAccuracy.toFloat())
            .putInt(KEY_GAMES_PLAYED, newGamesPlayed)
            .putInt(KEY_TOTAL_SAFE, newTotalSafe)
            .putInt(KEY_TOTAL_DANGER, newTotalDanger)
            .apply()

        val updated = GameStatistics(
            bestScore = newBestScore,
            bestStreak = newBestStreak,
            bestAccuracy = newBestAccuracy,
            gamesPlayed = newGamesPlayed,
            totalSafeSelections = newTotalSafe,
            totalDangerSelections = newTotalDanger
        )
        _statistics.value = updated

        return Pair(isNewBestScore, isNewBestStreak)
    }

    /**
     * Resets statistics to zero, preserving user preferences.
     */
    fun resetStatistics() {
        prefs.edit()
            .putInt(KEY_BEST_SCORE, 0)
            .putInt(KEY_BEST_STREAK, 0)
            .putFloat(KEY_BEST_ACCURACY, 0f)
            .putInt(KEY_GAMES_PLAYED, 0)
            .putInt(KEY_TOTAL_SAFE, 0)
            .putInt(KEY_TOTAL_DANGER, 0)
            .apply()

        _statistics.value = GameStatistics(
            bestScore = 0,
            bestStreak = 0,
            bestAccuracy = 0.0,
            gamesPlayed = 0,
            totalSafeSelections = 0,
            totalDangerSelections = 0
        )
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
        _hapticsEnabled.value = enabled
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setDifficulty(diff: Difficulty) {
        prefs.edit().putString(KEY_DIFFICULTY, diff.name).apply()
        _difficulty.value = diff
    }

    companion object {
        private const val PREFS_NAME = "safe_number_preferences"
        private const val KEY_BEST_SCORE = "key_best_score"
        private const val KEY_BEST_STREAK = "key_best_streak"
        private const val KEY_BEST_ACCURACY = "key_best_accuracy"
        private const val KEY_GAMES_PLAYED = "key_games_played"
        private const val KEY_TOTAL_SAFE = "key_total_safe"
        private const val KEY_TOTAL_DANGER = "key_total_danger"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_DIFFICULTY = "key_difficulty"
    }
}
