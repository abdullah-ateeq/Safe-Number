package com.example.safenumber.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.safenumber.audio.HapticController
import com.example.safenumber.audio.SoundPlayer
import com.example.safenumber.data.generator.OptionGenerator
import com.example.safenumber.data.generator.PatternGenerator
import com.example.safenumber.data.model.Difficulty
import com.example.safenumber.data.model.GameOverReason
import com.example.safenumber.data.model.GameState
import com.example.safenumber.data.model.NumberPattern
import com.example.safenumber.data.model.PatternType
import com.example.safenumber.data.model.SafeNumberResult
import com.example.safenumber.data.repository.PreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

class SafeNumberViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository(application)
    val soundPlayer = SoundPlayer()
    val hapticController = HapticController(application)

    private val _uiState = MutableStateFlow(SafeNumberUiState())
    val uiState: StateFlow<SafeNumberUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var countdownJob: Job? = null
    private var feedbackClearJob: Job? = null
    private var lastWarningTickSecond = -1
    private var previousPatternType: PatternType? = null

    init {
        // Collect repository states
        viewModelScope.launch {
            repository.statistics.collect { stats ->
                _uiState.update { it.copy(statistics = stats) }
            }
        }
        viewModelScope.launch {
            repository.soundEnabled.collect { enabled ->
                soundPlayer.isEnabled = enabled
                _uiState.update { it.copy(soundEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            repository.hapticsEnabled.collect { enabled ->
                hapticController.isEnabled = enabled
                _uiState.update { it.copy(hapticsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            repository.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            repository.difficulty.collect { diff ->
                _uiState.update { it.copy(difficulty = diff) }
            }
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        repository.setDifficulty(difficulty)
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        repository.setSoundEnabled(enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        repository.setHapticsEnabled(enabled)
    }

    fun setThemeMode(mode: String) {
        repository.setThemeMode(mode)
    }

    fun resetStatistics() {
        repository.resetStatistics()
    }

    /**
     * Starts a new game session with countdown.
     */
    fun startGame(difficulty: Difficulty = _uiState.value.difficulty) {
        cancelAllJobs()
        setDifficulty(difficulty)

        previousPatternType = null
        val firstPattern = PatternGenerator.generatePattern(difficulty, null)
        previousPatternType = firstPattern.type
        val firstOptions = OptionGenerator.generateOptions(
            firstPattern.dangerousNumber,
            firstPattern,
            difficulty.optionCount
        )

        _uiState.update {
            it.copy(
                gameState = GameState.COUNTDOWN,
                difficulty = difficulty,
                currentPattern = firstPattern,
                currentOptions = firstOptions,
                score = 0,
                streak = 0,
                safeSelections = 0,
                dangerSelections = 0,
                remainingTime = difficulty.initialTimeSeconds,
                maxTime = difficulty.maxTimeSeconds,
                countdownNumber = 3,
                countdownText = "3",
                lastTappedNumber = null,
                lastTappedWasSafe = null,
                feedbackMessage = null,
                feedbackPoints = null,
                milestoneMessage = null,
                showRestartDialog = false,
                showExitDialog = false
            )
        }

        startCountdown()
    }

    private fun startCountdown() {
        countdownJob = viewModelScope.launch {
            for (step in 3 downTo 1) {
                _uiState.update { it.copy(countdownNumber = step, countdownText = step.toString()) }
                soundPlayer.playCountdownTick(isFinal = false)
                delay(650)
            }
            _uiState.update { it.copy(countdownNumber = 0, countdownText = "GO!") }
            soundPlayer.playCountdownTick(isFinal = true)
            delay(400)

            _uiState.update {
                it.copy(
                    gameState = GameState.PLAYING,
                    remainingTime = it.difficulty.initialTimeSeconds
                )
            }
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        lastWarningTickSecond = -1
        timerJob = viewModelScope.launch {
            val stepMs = 50L
            while (_uiState.value.gameState == GameState.PLAYING) {
                delay(stepMs)
                val current = _uiState.value.remainingTime
                val updated = current - (stepMs / 1000f)

                if (updated <= 0f) {
                    _uiState.update { it.copy(remainingTime = 0f) }
                    handleTimeout()
                    break
                } else {
                    _uiState.update { it.copy(remainingTime = updated) }
                    // Warning tick at <= 3.0s
                    if (updated <= 3.0f) {
                        val currentSec = updated.toInt()
                        if (currentSec != lastWarningTickSecond) {
                            lastWarningTickSecond = currentSec
                            soundPlayer.playWarningTick()
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles tapping an option button.
     */
    fun selectOption(selectedNumber: Int) {
        val state = _uiState.value
        if (state.gameState != GameState.PLAYING) return

        val pattern = state.currentPattern ?: return
        val dangerous = pattern.dangerousNumber

        if (selectedNumber == dangerous) {
            // Player tapped the dangerous number!
            handleDangerSelected(selectedNumber, pattern)
        } else {
            // Player successfully tapped a safe number!
            handleSafeSelected(selectedNumber)
        }
    }

    private fun handleSafeSelected(selectedNumber: Int) {
        val current = _uiState.value
        val newStreak = current.streak + 1
        val newSafeCount = current.safeSelections + 1

        // Deterministic scoring formula:
        // basePoints = 100
        // multiplier = 1 + min(streak - 1, 20) * 0.05
        // speedBonus = (remainingTime * 10).toInt()
        val basePoints = 100
        val multiplier = 1.0 + (min((newStreak - 1).coerceAtLeast(0), 20) * 0.05)
        val speedBonus = (current.remainingTime * 10).toInt().coerceAtLeast(0)
        var roundScore = (basePoints * multiplier).toInt() + speedBonus

        // Streak bonus every 10 rounds: +250
        var bonusMessage: String? = null
        if (newStreak % 10 == 0) {
            roundScore += 250
            bonusMessage = "PATTERN STREAK! +250"
        }

        // Milestone messages
        val milestone = when (newStreak) {
            5 -> "GOOD!"
            10 -> "GREAT!"
            20 -> "PATTERN PRO!"
            30 -> "UNSTOPPABLE!"
            50 -> "SAFE NUMBER MASTER!"
            else -> null
        }

        val newTotalScore = current.score + roundScore

        // Time bonus
        val newTime = (current.remainingTime + current.difficulty.bonusTimeSeconds)
            .coerceAtMost(current.difficulty.maxTimeSeconds)

        // Generate next round
        val nextPattern = PatternGenerator.generatePattern(current.difficulty, previousPatternType)
        previousPatternType = nextPattern.type
        val nextOptions = OptionGenerator.generateOptions(
            nextPattern.dangerousNumber,
            nextPattern,
            current.difficulty.optionCount
        )

        _uiState.update {
            it.copy(
                score = newTotalScore,
                streak = newStreak,
                safeSelections = newSafeCount,
                remainingTime = newTime,
                currentPattern = nextPattern,
                currentOptions = nextOptions,
                lastTappedNumber = selectedNumber,
                lastTappedWasSafe = true,
                feedbackMessage = bonusMessage ?: "SAFE!",
                feedbackPoints = roundScore,
                milestoneMessage = milestone
            )
        }

        if (milestone != null || bonusMessage != null) {
            soundPlayer.playMilestoneSound()
            hapticController.vibrateMilestone()
        } else {
            soundPlayer.playSafeSound()
            hapticController.vibrateSafeChoice()
        }

        scheduleFeedbackClear()
    }

    private fun handleDangerSelected(selectedNumber: Int, pattern: NumberPattern) {
        timerJob?.cancel()
        soundPlayer.playDangerSound()
        hapticController.vibrateDanger()

        val current = _uiState.value
        val newDangerCount = current.dangerSelections + 1
        val total = current.safeSelections + newDangerCount
        val finalAccuracy = if (total == 0) 100.0 else (current.safeSelections.toDouble() / total) * 100.0

        val (isNewBestScore, isNewBestStreak) = repository.recordGameOutcome(
            score = current.score,
            streak = current.streak,
            gameSafeCount = current.safeSelections,
            gameDangerCount = newDangerCount,
            gameAccuracy = finalAccuracy
        )

        val result = SafeNumberResult(
            difficulty = current.difficulty,
            score = current.score,
            streak = current.streak,
            safeSelections = current.safeSelections,
            accuracy = finalAccuracy,
            reason = GameOverReason.DANGER_SELECTED,
            pattern = pattern,
            tappedNumber = selectedNumber,
            isNewBestScore = isNewBestScore,
            isNewBestStreak = isNewBestStreak
        )

        _uiState.update {
            it.copy(
                gameState = GameState.GAME_OVER,
                dangerSelections = newDangerCount,
                lastTappedNumber = selectedNumber,
                lastTappedWasSafe = false,
                lastResult = result,
                feedbackMessage = "DANGER!"
            )
        }
    }

    private fun handleTimeout() {
        timerJob?.cancel()
        soundPlayer.playGameOverSound()
        hapticController.vibrateGameOver()

        val current = _uiState.value
        val total = current.safeSelections + current.dangerSelections
        val finalAccuracy = if (total == 0) 100.0 else (current.safeSelections.toDouble() / total) * 100.0

        val (isNewBestScore, isNewBestStreak) = repository.recordGameOutcome(
            score = current.score,
            streak = current.streak,
            gameSafeCount = current.safeSelections,
            gameDangerCount = current.dangerSelections,
            gameAccuracy = finalAccuracy
        )

        val result = SafeNumberResult(
            difficulty = current.difficulty,
            score = current.score,
            streak = current.streak,
            safeSelections = current.safeSelections,
            accuracy = finalAccuracy,
            reason = GameOverReason.TIMEOUT,
            pattern = current.currentPattern,
            tappedNumber = null,
            isNewBestScore = isNewBestScore,
            isNewBestStreak = isNewBestStreak
        )

        _uiState.update {
            it.copy(
                gameState = GameState.GAME_OVER,
                lastResult = result,
                feedbackMessage = "TIME'S UP"
            )
        }
    }

    private fun scheduleFeedbackClear() {
        feedbackClearJob?.cancel()
        feedbackClearJob = viewModelScope.launch {
            delay(1200)
            _uiState.update {
                it.copy(
                    lastTappedNumber = null,
                    lastTappedWasSafe = null,
                    feedbackMessage = null,
                    feedbackPoints = null,
                    milestoneMessage = null
                )
            }
        }
    }

    fun pauseGame() {
        if (_uiState.value.gameState == GameState.PLAYING) {
            timerJob?.cancel()
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            _uiState.update { it.copy(gameState = GameState.PLAYING) }
            startTimer()
        }
    }

    fun showRestartConfirmation(show: Boolean) {
        _uiState.update { it.copy(showRestartDialog = show) }
    }

    fun showExitConfirmation(show: Boolean) {
        _uiState.update { it.copy(showExitDialog = show) }
    }

    fun restartCurrentGame() {
        _uiState.update { it.copy(showRestartDialog = false) }
        startGame(_uiState.value.difficulty)
    }

    fun exitGameToHome() {
        cancelAllJobs()
        _uiState.update {
            it.copy(
                gameState = GameState.READY,
                showRestartDialog = false,
                showExitDialog = false
            )
        }
    }

    private fun cancelAllJobs() {
        timerJob?.cancel()
        countdownJob?.cancel()
        feedbackClearJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllJobs()
    }
}
