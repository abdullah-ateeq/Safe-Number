package com.example.safenumber.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenumber.ui.theme.AccentBlue
import com.example.safenumber.ui.theme.DangerRed
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    // Splash animation sequence: 2 -> 4 -> 6 -> 8 -> 10 (danger highlighted)
    var step by remember { mutableIntStateOf(0) }
    var highlightDanger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        step = 1 // 2
        delay(180)
        step = 2 // 4
        delay(180)
        step = 3 // 6
        delay(180)
        step = 4 // 8
        delay(200)
        step = 5 // 10
        highlightDanger = true
        delay(350)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "SAFE NUMBER",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Sequence demonstration
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val numbers = listOf("2", "4", "6", "8")
                numbers.forEachIndexed { index, num ->
                    val isVisible = step > index
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn()
                    ) {
                        SplashNumberBox(
                            text = num,
                            isHighlighted = false
                        )
                    }
                }

                // Final number or question mark
                if (step < 5) {
                    SplashNumberBox(
                        text = "?",
                        isHighlighted = false,
                        isTarget = true
                    )
                } else {
                    SplashNumberBox(
                        text = "10",
                        isHighlighted = highlightDanger,
                        isDanger = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Find the pattern.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Avoid the danger.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlightDanger) DangerRed else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SplashNumberBox(
    text: String,
    isHighlighted: Boolean,
    isTarget: Boolean = false,
    isDanger: Boolean = false
) {
    val shape = RoundedCornerShape(10.dp)
    val bgColor = when {
        isDanger && isHighlighted -> DangerRed
        isTarget -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isDanger && isHighlighted -> Color.White
        isTarget -> AccentBlue
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .sizeIn(minWidth = 46.dp, minHeight = 52.dp)
            .clip(shape)
            .background(bgColor)
            .border(
                width = if (isHighlighted || isTarget) 2.dp else 1.dp,
                color = if (isDanger && isHighlighted) DangerRed else if (isTarget) AccentBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = shape
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
