package com.example.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CalculatorEvent
import com.example.CalculatorViewModel
import com.example.ui.theme.ObsidianDanger
import com.example.ui.theme.ObsidianDangerDim
import com.example.ui.theme.ObsidianPrimary
import com.example.ui.theme.ObsidianPrimaryDim
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun DisplayArea(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    
    // Blinking cursor
    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(state.cursorPosition, state.expression) {
        cursorVisible = true
        while (true) {
            delay(500)
            cursorVisible = !cursorVisible
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { /* handle */ }
                ) { change, dragAmount ->
                    change.consume()
                    if (abs(dragAmount.x) > abs(dragAmount.y)) {
                        if (dragAmount.x < -20) {
                            viewModel.onEvent(CalculatorEvent.Backspace)
                        } else if (dragAmount.x > 20) {
                            viewModel.onEvent(CalculatorEvent.Redo)
                        }
                    } else {
                        if (dragAmount.y > 20) {
                            viewModel.onEvent(CalculatorEvent.Clear)
                        } else if (dragAmount.y < -20) {
                            viewModel.onEvent(CalculatorEvent.TiltNotch)
                        }
                    }
                }
            }
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // n= badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = (-40).dp)
                .background(ObsidianPrimaryDim, RoundedCornerShape(8.dp))
                .border(1.dp, ObsidianPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "n=${state.itemCount}",
                color = ObsidianPrimary,
                fontSize = 13.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }

        // Invert switch
        AnimatedVisibility(
            visible = state.showInvertSwitch,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-85).dp)
        ) {
            Box(
                modifier = Modifier
                    .background(ObsidianDangerDim, RoundedCornerShape(20.dp))
                    .border(1.dp, ObsidianDanger, RoundedCornerShape(20.dp))
                    .clickable { viewModel.onEvent(CalculatorEvent.InvertExpression) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🔄 FLIP",
                    color = ObsidianDanger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            // Expression Editor
            Text(
                text = buildAnnotatedString {
                    val expr = state.expression.replace("*", "×").replace("/", "÷")
                    if (expr.isEmpty()) {
                        if (cursorVisible) {
                            withStyle(SpanStyle(color = ObsidianPrimary, background = ObsidianPrimary)) {
                                append("|")
                            }
                        }
                    } else {
                        for (i in 0..expr.length) {
                            if (i == state.cursorPosition && cursorVisible) {
                                withStyle(SpanStyle(color = ObsidianPrimary, background = ObsidianPrimary)) {
                                    append("|")
                                }
                            }
                            if (i < expr.length) {
                                append(expr[i])
                            }
                        }
                    }
                },
                fontSize = 28.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color(0xFF888888),
                textAlign = TextAlign.End,
                lineHeight = 36.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Result
            Text(
                text = state.result,
                fontSize = 65.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (state.isFinalized) ObsidianPrimary else if (state.showInvertSwitch) Color(0xFFFF6B6B) else Color.White,
                textAlign = TextAlign.End,
                lineHeight = 70.sp
            )
        }
    }
}
