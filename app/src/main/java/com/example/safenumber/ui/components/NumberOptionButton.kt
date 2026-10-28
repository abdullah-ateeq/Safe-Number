package com.example.safenumber.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenumber.ui.theme.DangerRed
import com.example.safenumber.ui.theme.SafeGreen

@Composable
fun NumberOptionButton(
    number: Int,
    isLastTapped: Boolean,
    lastTappedWasSafe: Boolean?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(),
        label = "button_scale"
    )

    val targetBgColor = when {
        isLastTapped && lastTappedWasSafe == true -> SafeGreen
        isLastTapped && lastTappedWasSafe == false -> DangerRed
        else -> MaterialTheme.colorScheme.surface
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = spring(),
        label = "button_bg_color"
    )

    val targetTextColor = when {
        isLastTapped -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    val targetBorderColor = when {
        isLastTapped && lastTappedWasSafe == true -> SafeGreen
        isLastTapped && lastTappedWasSafe == false -> DangerRed
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedBgColor,
            disabledContainerColor = animatedBgColor.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.5.dp, targetBorderColor),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .scale(scale)
            .padding(4.dp)
    ) {
        Text(
            text = number.toString(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = targetTextColor
        )
    }
}
