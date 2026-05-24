package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.api.MockCasinoService
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ClickConsoleScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val autoclickerActive by viewModel.autoclickerActive.collectAsState()
    val logs by viewModel.autoclickerLogs.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedCasinoUrl by remember { mutableStateOf("https://api.apexspin-platinum.com/feed") }
    var casinoStatsResult by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isFetchingMetadata by remember { mutableStateOf(false) }

    val casinos = listOf(
        "https://api.apexspin-platinum.com/feed" to "ApexSpin Platinum Live",
        "https://api.royal-bet.com/v2/stream" to "BetRoyal Casino Live",
        "https://shield-prod.vegas-shield.com" to "VegasShield Premium",
        "https://nova-api.casino-io.net/v1" to "Novatrading Gaming API"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. BONO ACTIVATOR CONTROLLER ---
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
                        "DISPARADOR AUTOMÁTICO - CLICS DE BONO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberAmber,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (autoclickerActive) "AUTO-BETTING: CONECTADO" else "AUTOCLICKER: APAGADO",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (autoclickerActive) CyberGreen else TextWhite
                            )
                            Text(
                                "Simula clics en coordenadas óptimas para liberación rápida de rollover.",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }

                        Switch(
                            checked = autoclickerActive,
                            onCheckedChange = { viewModel.toggleAutoclicker() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberGreen,
                                checkedTrackColor = CyberGreen.copy(alpha = 0.4f),
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = LightSlate
                            ),
                            modifier = Modifier.testTag("autoclicker_switch")
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightSlate, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (autoclickerActive) CyberGreen else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (autoclickerActive) "SIMULANDO IMPULSOS - COORDENADAS DILATADAS" else "Estado inactivo - En espera de click",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                            }
                            Text(
                                "El algoritmo añade un retardo variable aleatorio (entropy shift) de 4-6 segundos entre pulsos para bypass de supervisores de juego.",
                                fontSize = 11.sp,
                                color = TextGray,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 2. MULTIPLE CASINO CONNECTIONS (requests.get python counterpart) ---
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
                    Text(
                        "CONEXIÓN MANUAL & SCRAPERS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        "Seleccione una dirección de API para probar la carga directa del metadata y verificar su integridad de integración:",
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        casinos.forEach { (url, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedCasinoUrl == url) LightSlate else Color.Transparent)
                                    .clickable { selectedCasinoUrl = url }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text(url, fontSize = 9.sp, color = TextGray, fontFamily = FontFamily.Monospace)
                                }
                                RadioButton(
                                    selected = selectedCasinoUrl == url,
                                    onClick = { selectedCasinoUrl = url },
                                    colors = RadioButtonDefaults.colors(selectedColor = CyberCyan)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isFetchingMetadata = true
                                casinoStatsResult = MockCasinoService.fetchDirectCasinoMetadata(selectedCasinoUrl)
                                isFetchingMetadata = false
                                viewModel.addLogEntry("Metadatos descargados de: $selectedCasinoUrl")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = AbyssBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("inspect_casino_button")
                    ) {
                        if (isFetchingMetadata) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AbyssBlack, strokeWidth = 2.dp)
                        } else {
                            Text("Consultar Integridad de API", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (casinoStatsResult != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LightSlate, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Resultado Scraper Metadata (requests.json):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                            casinoStatsResult?.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(key, fontSize = 10.sp, color = TextGray, fontFamily = FontFamily.Monospace)
                                    Text(value.toString(), fontSize = 10.sp, color = TextWhite, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. CONSOLE OUTPUT ---
        item {
            Text(
                "TERMINAL DE EVENTOS & FILTRADO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(logs) { log ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightSlate, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = log,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (log.contains("GANADO") || log.contains("LOGRO")) CyberGreen else if (log.contains("ACTIVADOR") || log.contains("AUTO-CLICK")) CyberAmber else TextWhite
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
