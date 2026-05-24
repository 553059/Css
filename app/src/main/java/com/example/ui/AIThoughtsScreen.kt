package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AIThoughtsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val thoughts by viewModel.aiThoughts.collectAsState()
    val isGenerating by viewModel.isGeneratingAdvice.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. PROMPT RUNNING HEADER ---
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
                        "SISTEMA DE PENSAMIENTO AUTOMATIZADO - IA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        "Alimenta instantáneamente la IA de Gemini con la telemetría actual de la mesa (conteos blackjack, giros ruleta) para calcular tácticas de cobertura optimizadas contra la casa.",
                        fontSize = 12.sp,
                        color = TextWhite,
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = { viewModel.executeStrategicThinking() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = AbyssBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ai_thinking_button"),
                        enabled = !isGenerating
                    ) {
                        if (isGenerating) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = AbyssBlack,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Calculando Vector de Riesgo...", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Consultar al Pensador Estratégico IA", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isGenerating) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightSlate)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "El motor está estructurando los parámetros... Esto suele tardar entre 2 y 5 segundos.",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }

        // --- 2. RESPONSIBLE PLAY DISCLAIMER ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberAmber.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Riesgo",
                        tint = CyberAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            "ADVERTENCIA DE RIESGO DE APUESTA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberAmber,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "Los algoritmos predictivos y el conteo de cartas son aproximaciones matemáticas que dependen de la uniformidad estadística de la casa. Nunca arriesgue capital que no pueda permitirse perder.",
                            fontSize = 11.sp,
                            color = TextWhite.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // --- 3. PREVIOUS STRATEGIC THOUGHTS LOGS ---
        if (thoughts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Presione el botón superior para calcular sugerencias probabilísticas personalizadas en base a su sesión.",
                        color = TextGray,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            items(thoughts, key = { it.id }) { thought ->
                val riskColor = when (thought.calculatedRiskRating) {
                    "LOW" -> CyberGreen
                    "MEDIUM" -> CyberCyan
                    else -> CyberAmber
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateGray),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LightSlate, RoundedCornerShape(16.dp))
                        .testTag("ai_thought_card_${thought.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VECTOR DIRECTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = riskColor,
                                fontFamily = FontFamily.Monospace
                            )

                            Box(
                                modifier = Modifier
                                    .background(riskColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, riskColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "RIESGO DE AMORTIZACIÓN: ${thought.calculatedRiskRating}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = "Entorno Evaluado: ${thought.gameContext}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )

                        HorizontalDivider(color = LightSlate, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = thought.thoughtText,
                            fontSize = 13.sp,
                            color = TextWhite,
                            lineHeight = 18.sp
                        )

                        if (thought.suggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Plan Táctico Recomendado:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                            Text(
                                text = thought.suggestions,
                                fontSize = 12.sp,
                                color = TextGray,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
