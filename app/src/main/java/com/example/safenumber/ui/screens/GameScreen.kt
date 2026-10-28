package com.example.safenumber.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenumber.data.model.GameState
import com.example.safenumber.ui.components.NumberOptionButton
import com.example.safenumber.ui.components.PauseDialog
import com.example.safenumber.ui.components.SequenceDisplay
import com.example.safenumber.ui.components.SimpleConfirmDialog
import com.example.safenumber.ui.components.StreakMilestoneBanner
import com.example.safenumber.ui.components.TimerBar
import com.example.safenumber.ui.theme.AccentBlue
import com.example.safenumber.ui.viewmodel.SafeNumberUiState
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    uiState: SafeNumberUiState,
    onOptionSelected: (Int) -> Unit,
    onPauseClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onRestartRequested: () -> Unit,
    onConfirmRestart: () -> Unit,
    onDismissRestart: () -> Unit,
    onExitRequested: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExit: () -> Unit,
    onGameOverTransition: () -> Unit
) {
    // Intercept hardware/gesture back press during gameplay
    BackHandler(enabled = uiState.gameState == GameState.PLAYING || uiState.gameState == GameState.PAUSED) {
        if (uiState.gameState == GameState.PLAYING) {
            onPauseClicked()
        } else {
            onExitRequested()
        }
    }

    // Auto navigate to result when game over
    LaunchedEffect(uiState.gameState) {
        if (uiState.gameState == GameState.GAME_OVER) {
            // Brief 600ms pause so the player sees the red danger feedback or timeout
            delay(650)
            onGameOverTransition()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: STREAK, SCORE, PAUSE BUTTON
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STREAK ${uiState.streak}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SCORE ${uiState.score}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onPauseClicked,
                        enabled = uiState.gameState == GameState.PLAYING,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = "Pause",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Timer bar
                TimerBar(
                    remainingTime = uiState.remainingTime,
                    maxTime = uiState.maxTime,
                    isUrgent = uiState.isUrgentTime
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Game Header: AVOID THE NEXT NUMBER
                Text(
                    text = "AVOID THE NEXT NUMBER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.2.sp,
                    color = AccentBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Feedback Banner (SAFE! / STREAK / MILESTONE)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StreakMilestoneBanner(
                        message = uiState.feedbackMessage,
                        points = uiState.feedbackPoints,
                        milestoneMessage = uiState.milestoneMessage
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sequence Display: e.g. 3   6   9   12   ?
                val seq = uiState.currentPattern?.sequence ?: emptyList()
                SequenceDisplay(sequence = seq)

                Spacer(modifier = Modifier.weight(1f))

                // Option Buttons Grid
                val options = uiState.currentOptions
                val isPlaying = uiState.gameState == GameState.PLAYING

                if (options.size <= 4) {
                    // 2x2 grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (options.isNotEmpty()) {
                            NumberOptionButton(
                                number = options[0],
                                isLastTapped = uiState.lastTappedNumber == options[0],
                                lastTappedWasSafe = uiState.lastTappedWasSafe,
                                enabled = isPlaying,
                                onClick = { onOptionSelected(options[0]) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (options.size > 1) {
                            NumberOptionButton(
                                number = options[1],
                                isLastTapped = uiState.lastTappedNumber == options[1],
                                lastTappedWasSafe = uiState.lastTappedWasSafe,
                                enabled = isPlaying,
                                onClick = { onOptionSelected(options[1]) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (options.size > 2) {
                            NumberOptionButton(
                                number = options[2],
                                isLastTapped = uiState.lastTappedNumber == options[2],
                                lastTappedWasSafe = uiState.lastTappedWasSafe,
                                enabled = isPlaying,
                                onClick = { onOptionSelected(options[2]) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (options.size > 3) {
                            NumberOptionButton(
                                number = options[3],
                                isLastTapped = uiState.lastTappedNumber == options[3],
                                lastTappedWasSafe = uiState.lastTappedWasSafe,
                                enabled = isPlaying,
                                onClick = { onOptionSelected(options[3]) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    // 5 options layout (for Hard mode)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NumberOptionButton(
                            number = options[0],
                            isLastTapped = uiState.lastTappedNumber == options[0],
                            lastTappedWasSafe = uiState.lastTappedWasSafe,
                            enabled = isPlaying,
                            onClick = { onOptionSelected(options[0]) },
                            modifier = Modifier.weight(1f)
                        )
                        NumberOptionButton(
                            number = options[1],
                            isLastTapped = uiState.lastTappedNumber == options[1],
                            lastTappedWasSafe = uiState.lastTappedWasSafe,
                            enabled = isPlaying,
                            onClick = { onOptionSelected(options[1]) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NumberOptionButton(
                            number = options[2],
                            isLastTapped = uiState.lastTappedNumber == options[2],
                            lastTappedWasSafe = uiState.lastTappedWasSafe,
                            enabled = isPlaying,
                            onClick = { onOptionSelected(options[2]) },
                            modifier = Modifier.weight(1f)
                        )
                        NumberOptionButton(
                            number = options[3],
                            isLastTapped = uiState.lastTappedNumber == options[3],
                            lastTappedWasSafe = uiState.lastTappedWasSafe,
                            enabled = isPlaying,
                            onClick = { onOptionSelected(options[3]) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        NumberOptionButton(
                            number = options[4],
                            isLastTapped = uiState.lastTappedNumber == options[4],
                            lastTappedWasSafe = uiState.lastTappedWasSafe,
                            enabled = isPlaying,
                            onClick = { onOptionSelected(options[4]) },
                            modifier = Modifier.fillMaxWidth(0.55f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Countdown Overlay
            AnimatedVisibility(
                visible = uiState.gameState == GameState.COUNTDOWN,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.countdownText,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            color = AccentBlue
                        )
                    }
                }
            }

            // Pause Dialog
            if (uiState.gameState == GameState.PAUSED && !uiState.showRestartDialog && !uiState.showExitDialog) {
                PauseDialog(
                    onResume = onResumeClicked,
                    onRestart = onRestartRequested,
                    onExit = onExitRequested
                )
            }

            // Restart Confirmation Dialog
            if (uiState.showRestartDialog) {
                SimpleConfirmDialog(
                    title = "Restart this game?",
                    message = "Current progress will be lost.",
                    confirmText = "RESTART",
                    cancelText = "CANCEL",
                    isDestructive = true,
                    onConfirm = onConfirmRestart,
                    onDismiss = onDismissRestart
                )
            }

            // Exit Confirmation Dialog
            if (uiState.showExitDialog) {
                SimpleConfirmDialog(
                    title = "Exit game?",
                    message = "Current progress will be lost.",
                    confirmText = "EXIT",
                    cancelText = "CANCEL",
                    isDestructive = true,
                    onConfirm = onConfirmExit,
                    onDismiss = onDismissExit
                )
            }
        }
    }
}
