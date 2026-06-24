package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.components.CalculatorButton
import com.example.components.DisplayArea
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBackground
                ) {
                    val viewModel: CalculatorViewModel = viewModel()
                    val state by viewModel.state.collectAsState()

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = ObsidianBackground
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        ) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ULTIMATH",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp,
                                        color = ObsidianTextMain
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(ObsidianPrimaryDim, RoundedCornerShape(4.dp))
                                            .border(1.dp, ObsidianPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "OBSIDIAN X",
                                            fontSize = 10.sp,
                                            color = ObsidianPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                // Menu Trigger
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.Transparent, RoundedCornerShape(12.dp))
                                        .border(1.dp, ObsidianGlassBorder, RoundedCornerShape(12.dp))
                                        .clickable { /* Toggle Launcher */ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        tint = ObsidianPrimary
                                    )
                                }
                            }

                            // Memory/Notch Badge (if active)
                            AnimatedVisibility(visible = state.notchMemory != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(ObsidianPrimaryDim, RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                                            .padding(8.dp)
                                            .clickable { viewModel.onEvent(CalculatorEvent.TiltNotch) }
                                    ) {
                                        Text("MEMORY: ${state.notchMemory}", color = ObsidianPrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                            
                            // Soul Jar
                            AnimatedVisibility(visible = state.soulJarItem != null) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 24.dp)
                                        .size(55.dp)
                                        .background(Color(0x33FFFFFF), CircleShape)
                                        .border(1.dp, ObsidianPrimary, CircleShape)
                                        .clickable { viewModel.onEvent(CalculatorEvent.RestoreSoul) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("♻️", fontSize = 16.sp)
                                        Text(
                                            text = state.soulJarItem ?: "",
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Display Area
                            DisplayArea(
                                viewModel = viewModel,
                                modifier = Modifier.weight(1f)
                            )

                            // Controls Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    TextButton(onClick = { /* History */ }) { Text("Logs", color = ObsidianTextSecondary) }
                                    TextButton(onClick = { /* Audit */ }) { Text("Audit", color = ObsidianTextSecondary) }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                                    TextButton(onClick = { /* Explain */ }) { Text("Explain", color = ObsidianPrimary) }
                                    TextButton(onClick = { viewModel.onEvent(CalculatorEvent.Undo) }) { Text("Undo", color = ObsidianTextSecondary) }
                                    TextButton(onClick = { viewModel.onEvent(CalculatorEvent.Redo) }) { Text("Redo", color = ObsidianTextSecondary) }
                                    TextButton(onClick = { viewModel.onEvent(CalculatorEvent.Backspace) }) { Text("Del", color = ObsidianDanger) }
                                }
                            }

                            // Keypad
                            val keys = listOf(
                                listOf("C", "()", "%", "/"),
                                listOf("7", "8", "9", "*"),
                                listOf("4", "5", "6", "-"),
                                listOf("1", "2", "3", "+"),
                                listOf("neg", "0", ".", "=")
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                                    .padding(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (row in keys) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false), // Fixed for AspectRatio
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        for (key in row) {
                                            val isPrimary = key == "="
                                            val isOp = key in listOf("/", "*", "-", "+", "C", "()", "%", "neg")
                                            val textColor = when {
                                                key == "C" -> ObsidianDanger
                                                isOp -> ObsidianPrimary
                                                else -> ObsidianTextMain
                                            }
                                            
                                            CalculatorButton(
                                                text = if (key == "neg") "+/-" else if (key == "*") "×" else if (key == "/") "÷" else key,
                                                textColor = textColor,
                                                isPrimary = isPrimary,
                                                modifier = Modifier.weight(1f).aspectRatio(1.2f),
                                                onClick = {
                                                    when (key) {
                                                        "C" -> viewModel.onEvent(CalculatorEvent.Clear)
                                                        "=" -> viewModel.onEvent(CalculatorEvent.Finalize)
                                                        "neg" -> viewModel.onEvent(CalculatorEvent.InsertToken("-"))
                                                        "()" -> {
                                                            val ex = state.expression
                                                            val openCount = ex.count { it == '(' }
                                                            val closeCount = ex.count { it == ')' }
                                                            if (openCount == closeCount) viewModel.onEvent(CalculatorEvent.InsertToken("("))
                                                            else viewModel.onEvent(CalculatorEvent.InsertToken(")"))
                                                        }
                                                        else -> viewModel.onEvent(CalculatorEvent.InsertToken(key))
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

