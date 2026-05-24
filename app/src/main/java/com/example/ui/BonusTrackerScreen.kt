package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActiveBonus
import com.example.ui.theme.*

@Composable
fun BonusTrackerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val bonuses by viewModel.activeBonuses.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }
    var casinoName by remember { mutableStateOf("") }
    var bonusName by remember { mutableStateOf("") }
    var bonusAmountStr by remember { mutableStateOf("100") }
    var multiplierStr by remember { mutableStateOf("30") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Title & Creation Toggle ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CONVERTIDOR & SEGUIMIENTO DE BONOS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberGreen,
                    fontFamily = FontFamily.Monospace
                )

                Button(
                    onClick = { showAddForm = !showAddForm },
                    colors = ButtonDefaults.buttonColors(containerColor = if (showAddForm) CyberAmber else CyberGreen, contentColor = AbyssBlack),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_bonus_form_toggle")
                ) {
                    Icon(
                        imageVector = if (showAddForm) Icons.Default.Delete else Icons.Default.Add,
                        contentDescription = "Toggle add",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showAddForm) "Cerrar" else "Nuevo Bono", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- ADD BONUS FORM ---
        item {
            AnimatedVisibility(
                visible = showAddForm,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
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
                            "REGISTRAR CONDICIONES DE NUEVO BONO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            fontFamily = FontFamily.Monospace
                        )

                        OutlinedTextField(
                            value = casinoName,
                            onValueChange = { casinoName = it },
                            label = { Text("Nombre del Casino") },
                            modifier = Modifier.fillMaxWidth().testTag("add_bonus_casino"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = LightSlate)
                        )

                        OutlinedTextField(
                            value = bonusName,
                            onValueChange = { bonusName = it },
                            label = { Text("Identificador de Promoción (Bono)") },
                            modifier = Modifier.fillMaxWidth().testTag("add_bonus_name"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = LightSlate)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = bonusAmountStr,
                                onValueChange = { bonusAmountStr = it },
                                label = { Text("Capital (USD)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("add_bonus_value"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = LightSlate)
                            )

                            OutlinedTextField(
                                value = multiplierStr,
                                onValueChange = { multiplierStr = it },
                                label = { Text("Requisito (Rollover x)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("add_bonus_multiplier"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = LightSlate)
                            )
                        }

                        Button(
                            onClick = {
                                val amount = bonusAmountStr.toDoubleOrNull() ?: 100.0
                                val multiplier = multiplierStr.toDoubleOrNull() ?: 30.0
                                if (casinoName.isNotBlank() && bonusName.isNotBlank()) {
                                    viewModel.addNewActiveBonus(casinoName, bonusName, amount, multiplier)
                                    // Reset inputs
                                    casinoName = ""
                                    bonusName = ""
                                    showAddForm = false
                                } else {
                                    viewModel.addLogEntry("[ERROR] Rellene todos los campos para agregar un nuevo bono.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = AbyssBlack),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("add_bonus_confirm_button")
                        ) {
                            Text("Activar & Registrar Bono", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- LIST OF BONUSES ---
        if (bonuses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No se registran promociones ni bonos activos en la base de datos.",
                        color = TextGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(bonuses, key = { it.id }) { bonus ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateGray),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (bonus.isCompleted) CyberGreen.copy(alpha = 0.5f) else LightSlate,
                            RoundedCornerShape(16.dp)
                        ).testTag("bonus_card_${bonus.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    bonus.casinoName.uppercase(),
                                    fontSize = 11.sp,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    bonus.bonusName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                            
                            // Delete button
                            IconButton(
                                onClick = { viewModel.deleteBonusById(bonus) },
                                modifier = Modifier.size(24.dp).testTag("delete_bonus_${bonus.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar Bono",
                                    tint = CyberAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Quantitative breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Monto Inicial", fontSize = 10.sp, color = TextGray)
                                Text("$%.2f USD".format(bonus.amount), fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Multiplicador", fontSize = 10.sp, color = TextGray)
                                Text("x%.0f".format(bonus.wageringRequirementMultiplier), fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Total Objetivo", fontSize = 10.sp, color = TextGray)
                                Text("$%.2f USD".format(bonus.targetWagerAmount), fontSize = 13.sp, color = CyberGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Progress bar & completion ratio
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Progreso de Juego Amortizado",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                                Text(
                                    "%.1f%%".format(bonus.progressFraction * 100f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (bonus.isCompleted) CyberGreen else CyberCyan
                                )
                            }

                            // Linear indicator
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LightSlate)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(bonus.progressFraction)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (bonus.isCompleted) CyberGreen else CyberCyan)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Apostado: $%.1f / $%.0f".format(bonus.currentWageredAmount, bonus.targetWagerAmount),
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    if (bonus.isCompleted) "REQUISITO SATISFECHO" else "Restan: $%.1f USD".format(bonus.remainingWager),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (bonus.isCompleted) CyberGreen else CyberAmber,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Claim trigger / simulator testing
                        if (!bonus.isCompleted) {
                            Button(
                                onClick = { viewModel.claimOrCompleteBonus(bonus) },
                                colors = ButtonDefaults.buttonColors(containerColor = LightSlate, contentColor = CyberGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("complete_bonus_trigger_${bonus.id}"),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Satis", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Completar Playthrough de inmediato", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
