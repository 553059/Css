package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val backgroundSimActive by viewModel.backgroundSimulationActive.collectAsState()
    val obfuscationActive by viewModel.obfuscationActive.collectAsState()
    val camouflagePIN by viewModel.camouflagePIN.collectAsState()

    var showConfirmPurge by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. PROCESS CAMOUFLAGE (STEALTH) ---
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
                        "OCULTACIÓN DE PROCESO (STEALTH MODE)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        "Reemplaza instantáneamente la interfaz de este software de análisis por una calculadora financiera de préstamos amortizados.",
                        fontSize = 12.sp,
                        color = TextWhite,
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = { viewModel.setCamouflageActive(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = AbyssBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("enter_stealth_button")
                    ) {
                        Text("Activar Camuflaje de Pantalla de inmediato", fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightSlate, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Código de desbloqueo predeterminado: $camouflagePIN. Ingréselo en el capital inicial de la Calculadora de Préstamos o pulse el icono de candado superior derecho para revertir el camuflaje.",
                            fontSize = 11.sp,
                            color = TextGray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // --- 2. SECURITY OBFUSCATION & SYSTEM BACKGROUNDS ---
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
                        "SEGURIDAD & TELEMETRÍA SUBTERRÁNEA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace
                    )

                    // Background Ingestion Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text("Simulador en Segundo Plano", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Asimila sucesivos aportes simulados cada 15 segundos para dar robustez analítica continua.", fontSize = 11.sp, color = TextGray)
                        }
                        Switch(
                            checked = backgroundSimActive,
                            onCheckedChange = { viewModel.setBackgroundSimulation(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = CyberCyan.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("toggle_background_sim")
                        )
                    }

                    HorizontalDivider(color = LightSlate)

                    // Obfuscation Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text("Cifrado Local de Base de Datos", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Ofusca y resguarda los registros de Room SQLite utilizando claves locales criptográficas simuladas.", fontSize = 11.sp, color = TextGray)
                        }
                        Switch(
                            checked = obfuscationActive,
                            onCheckedChange = { viewModel.setObfuscationActive(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = CyberCyan.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("toggle_obfuscation")
                        )
                    }
                }
            }
        }

        // --- 3. HARD RESET (PANIC RESET) ---
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
                        "CONTROLES DE PÁNICO (PURGA)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberAmber,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        "Elimina todo rastro local de sesiones ingresadas, estadísticas recopiladas, historial del Pensador IA, y conteos actuales inmediatamente.",
                        fontSize = 12.sp,
                        color = TextWhite,
                        lineHeight = 16.sp
                    )

                    if (!showConfirmPurge) {
                        Button(
                            onClick = { showConfirmPurge = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberAmber, contentColor = AbyssBlack),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("purge_data_button")
                        ) {
                            Text("Purgar Base de Datos Local (Reset)", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CyberAmber, RoundedCornerShape(8.dp))
                                .background(CyberAmber.copy(alpha = 0.05f))
                                .padding(12.dp)
                        ) {
                            Text(
                                "¡ADVERTENCIA: Esta acción es irreversible! ¿Desea confirmar el vaciado de datos?",
                                fontSize = 12.sp,
                                color = CyberAmber,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.purgeHistoryLogs()
                                        showConfirmPurge = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberAmber, contentColor = AbyssBlack),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).testTag("purge_confirm")
                                ) {
                                    Text("Confirmar Purga", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { showConfirmPurge = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = LightSlate, contentColor = TextWhite),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).testTag("purge_cancel")
                                ) {
                                    Text("Cancelar", fontSize = 11.sp)
                                }
                            }
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
