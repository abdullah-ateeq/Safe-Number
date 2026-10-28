package com.example.safenumber.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenumber.ui.theme.AccentBlue
import com.example.safenumber.ui.theme.WarningAmber

@Composable
fun SequenceDisplay(
    sequence: List<Int>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        sequence.forEach { num ->
            SequenceCard(text = num.toString(), isTarget = false)
        }
        SequenceCard(text = "?", isTarget = true)
    }
}

@Composable
private fun SequenceCard(
    text: String,
    isTarget: Boolean
) {
    val shape = RoundedCornerShape(12.dp)
    val bgColor = if (isTarget) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = if (isTarget) {
        AccentBlue
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    val textColor = if (isTarget) {
        WarningAmber
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .sizeIn(minWidth = 50.dp, minHeight = 56.dp)
            .clip(shape)
            .background(bgColor)
            .border(
                width = if (isTarget) 2.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTarget) 24.sp else 22.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
