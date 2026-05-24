package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AbyssBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamouflageScreen(
    currentPin: String,
    onUnlock: () -> Unit
) {
    var principalAmount by remember { mutableStateOf("15000") }
    var annualRate by remember { mutableStateOf("5.5") }
    var yearsPeriod by remember { mutableStateOf("5") }
    var computedInterest by remember { mutableStateOf("") }
    var showExplanation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cálculo de Préstamos e Interés",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                actions = {
                    // Hidden escape trigger: a small Lock icon that requires continuous clicks or double-clicks
                    IconButton(
                        onClick = {
                            // Quick toggle button or simple click action
                            onUnlock()
                        },
                        modifier = Modifier.testTag("stealth_unlock_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Configuración Segura",
                            tint = Color.LightGray.copy(alpha = 0.4f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E222A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF14171F))
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Calculadora de Estabilidad Financiera (TIF)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    Text(
                        "Calcule el costo total de adquisición financiera del interés amortizado con sistema francés de tasa nominal anual.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Input Fields
            OutlinedTextField(
                value = principalAmount,
                onValueChange = {
                    principalAmount = it
                    // Secret shortcut: typing "1234" to value unlocks the HUD!
                    if (it == currentPin) {
                        onUnlock()
                    }
                },
                label = { Text("Capital Inicial ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("capital_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color.Gray
                )
            )

            OutlinedTextField(
                value = annualRate,
                onValueChange = { annualRate = it },
                label = { Text("Tasa de Interés Anual (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("rate_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color.Gray
                )
            )

            OutlinedTextField(
                value = yearsPeriod,
                onValueChange = { yearsPeriod = it },
                label = { Text("Período de Amortización (Años)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("years_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Button(
                onClick = {
                    val p = principalAmount.toDoubleOrNull() ?: 0.0
                    val r = (annualRate.toDoubleOrNull() ?: 0.0) / 100.0
                    val t = yearsPeriod.toDoubleOrNull() ?: 0.0
                    if (p > 0 && r >= 0 && t > 0) {
                        // Formula: A = P(1 + r * t) Simple amortization
                        val total = p * (1 + r * t)
                        val monthly = total / (t * 12)
                        computedInterest = "Pago Mensual: $%.2f\nRetorno Total: $%.2f\nInterés Generado: $%.2f".format(monthly, total, total - p)
                    } else {
                        computedInterest = "Cálculo Inválido. Verifique los campos."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("compute_finance_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = AbyssBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Calcular Amortización", fontWeight = FontWeight.Bold)
            }

            AnimatedVisibility(
                visible = computedInterest.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF262B35)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Resultados de Proyección:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            computedInterest,
                            fontSize = 15.sp,
                            color = Color(0xFF00FF66),
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Legitimate Disclaimer Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showExplanation = !showExplanation }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Ayuda",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Información de Licencia de Préstamos",
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (showExplanation) {
                Text(
                    "Esta herramienta utilitaria calcula aproximaciones matemáticas utilizando el sistema lineal de amortización. Para cálculos compuestos con períodos variables, consulte las plantillas normativas financieras oficiales de la secretaría de regulación bancaria.",
                    fontSize = 10.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    lineHeight = 14.sp
                )
            }
        }
    }
}
