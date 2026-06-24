package com.example.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ObsidianGlassBorder
import com.example.ui.theme.ObsidianPrimary
import com.example.ui.theme.ObsidianPrimaryDim
import com.example.ui.theme.ObsidianSurface

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 50),
        label = "scale"
    )

    val bgColor = if (isPrimary) ObsidianPrimary else ObsidianSurface
    val contentColor = if (isPrimary) Color.Black else if (isPressed) ObsidianPrimary else textColor
    val borderColor = if (isPrimary) Color.Transparent else if (isPressed) ObsidianPrimaryDim else ObsidianGlassBorder

    val gradient = if (isPrimary) {
        Brush.linearGradient(colors = listOf(bgColor, bgColor))
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF222222),
                Color(0xFF0F0F0F)
            )
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(brush = gradient)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = if (isPrimary) 32.sp else 28.sp,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium
        )
    }
}
