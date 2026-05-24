package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.LiveCasinoEvent
import com.example.data.GameSession
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val blackjackCount by viewModel.blackjackRunningCount.collectAsState()
    val blackjackDecks by viewModel.blackjackDecksCount.collectAsState()
    val matchThreshold by viewModel.matchThreshold.collectAsState()
    val rouletteColors by viewModel.rouletteHistoryColors.collectAsState()
    val liveCasinoFeed by viewModel.liveCasinoFeed.collectAsState()
    val recentSessions by viewModel.gameSessions.collectAsState()
    val backgroundSimActive by viewModel.backgroundSimulationActive.collectAsState()

    // Calculate distributions
    val totalSpins = rouletteColors.size
    val redSpins = rouletteColors.count { it == "Red" }
    val blackSpins = rouletteColors.count { it == "Black" }
    val greenSpins = rouletteColors.count { it == "Green" }

    val redPercentage = if (totalSpins > 0) redSpins.toFloat() / totalSpins else 0f
    val blackPercentage = if (totalSpins > 0) blackSpins.toFloat() / totalSpins else 0f
    val greenPercentage = if (totalSpins > 0) greenSpins.toFloat() / totalSpins else 0f

    val trueCount = blackjackCount.toFloat() / blackjackDecks.coerceAtLeast(1)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. HEAD CONTROL CARD (Sensitivity & Match Threshold) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightSlate, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "FILTRO MULTIPROCESO & COINCIDENCIAS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("dashboard_title")
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (backgroundSimActive) CyberGreen.copy(alpha = 0.15f) else CyberAmber.copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (backgroundSimActive) CyberGreen else CyberAmber,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                if (backgroundSimActive) "TELEMETRÍA SEGUNDO PLANO ACTIVA" else "TELEMETRÍA EN PAUSA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (backgroundSimActive) CyberGreen else CyberAmber,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        "Umbral de Coincidencia Estadística: %d%%".format((matchThreshold * 100).toInt()),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )

                    Slider(
                        value = matchThreshold,
                        onValueChange = { viewModel.setMatchThreshold(it) },
                        valueRange = 0.40f..0.95f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberGreen,
                            activeTrackColor = CyberGreen,
                            inactiveTrackColor = LightSlate
                        ),
                        modifier = Modifier.testTag("match_threshold_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Umbral info",
                            tint = TextGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Ajuste el umbral para controlar la rigurosidad de coincidencia. Umbrales altos (75%+) filtran falsos positivos en las sugerencias de apuestas estratégicas.",
                            fontSize = 11.sp,
                            color = TextGray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // --- 2. BLACKJACK ENGINE ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightSlate, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "DETECTOR BLACKJACK - CONTEO HI-LO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = { viewModel.resetCardCount() },
                            modifier = Modifier.size(24.dp).testTag("reset_bj_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reiniciar Conteo",
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Value Display
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(LightSlate, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Cuenta Corriente", fontSize = 11.sp, color = TextGray)
                                Text(
                                    text = if (blackjackCount > 0) "+$blackjackCount" else blackjackCount.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (blackjackCount > 0) CyberGreen else if (blackjackCount < 0) CyberAmber else TextWhite,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.testTag("running_count_text")
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(LightSlate, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Cuenta Verdadera", fontSize = 11.sp, color = TextGray)
                                Text(
                                    text = "%.1f".format(trueCount),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (trueCount > 1.5) CyberGreen else TextWhite,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.testTag("true_count_text")
                                )
                            }
                        }
                    }

                    // Card Action Inputs
                    Text("Ingresar Cartas Distribuidas:", fontSize = 12.sp, color = TextGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.logCardValue("2") },
                            colors = ButtonDefaults.buttonColors(containerColor = LightSlate, contentColor = CyberGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("bj_low_button"),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Low (+1)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("2 - 6", fontSize = 9.sp, color = TextGray)
                            }
                        }

                        Button(
                            onClick = { viewModel.logCardValue("7") },
                            colors = ButtonDefaults.buttonColors(containerColor = LightSlate, contentColor = TextWhite),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("bj_neutral_button"),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Neutro (0)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("7 - 9", fontSize = 9.sp, color = TextGray)
                            }
                        }

                        Button(
                            onClick = { viewModel.logCardValue("10") },
                            colors = ButtonDefaults.buttonColors(containerColor = LightSlate, contentColor = CyberAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("bj_high_button"),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("High (-1)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("10 - Ace", fontSize = 9.sp, color = TextGray)
                            }
                        }
                    }
                }
            }
        }

        // --- 3. ROULETTE ENGINE ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightSlate, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "ANALIZADOR DE RULETA - SECTOR COLOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen,
                        fontFamily = FontFamily.Monospace
                    )

                    // Draw Horizontal Distribution Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Distribución de color (Muestra de %d)".format(totalSpins), fontSize = 12.sp, color = TextWhite)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(LightSlate)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                if (redPercentage > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(redPercentage)
                                            .background(CyberAmber),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("%.0f%%".format(redPercentage * 100), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (greenPercentage > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(greenPercentage)
                                            .background(CyberGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("%.0f%%".format(greenPercentage * 100), fontSize = 10.sp, color = AbyssBlack, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (blackPercentage > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(blackPercentage)
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("%.0f%%".format(blackPercentage * 100), fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Input Wheel Shortcut List
                    Text("Ingresar número caído:", fontSize = 12.sp, color = TextGray)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listOf(0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23)) { number ->
                            val isRedValue = listOf(32, 19, 21, 25, 34, 27, 36, 30, 23)
                            val bg = when {
                                number == 0 -> CyberGreen
                                isRedValue.contains(number) -> CyberAmber
                                else -> Color.Black
                            }
                            val textCol = if (number == 0) AbyssBlack else Color.White
                            val borderCol = if (number == 0) Color.Transparent else LightSlate

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.logRouletteSpin(number) }
                                    .testTag("roulette_sample_$number"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    number.toString(),
                                    color = textCol,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. INTEGRATION API / LIVE SCRAPER TELEMETRY ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightSlate, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TELEMETRÍA EN VIVIO - CASINOS INTEGRADOS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberGreen)
                        )
                    }

                    Text(
                        "Flujo directo de datos de plataformas de apuestas integradas mediante HTTP Scrapers. Calculando tendencias globales en tiempo real:",
                        fontSize = 11.sp,
                        color = TextGray,
                        lineHeight = 15.sp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .background(AbyssBlack, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (liveCasinoFeed.isEmpty()) {
                            Text(
                                "Estableciendo conexión HTTPS secuenciada...",
                                fontSize = 11.sp,
                                color = TextGray,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            liveCasinoFeed.take(4).forEach { event ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${event.casinoName} (${event.gameType})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberGreen,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        event.result,
                                        fontSize = 10.sp,
                                        color = TextWhite,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Spacing bottom
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
