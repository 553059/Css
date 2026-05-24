package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Full-bleed content

        setContent {
            MyApplicationTheme {
                val camouflageActive by viewModel.camouflageActive.collectAsState()
                val currentPin by viewModel.camouflagePIN.collectAsState()

                Crossfade(
                    targetState = camouflageActive,
                    animationSpec = tween(durationMillis = 350),
                    label = "CamouflageTransition"
                ) { isCamouflaged ->
                    if (isCamouflaged) {
                        CamouflageScreen(
                            currentPin = currentPin,
                            onUnlock = {
                                viewModel.setCamouflageActive(false)
                                viewModel.addLogEntry("Camuflaje revertido. Retorno exitoso al panel HUD.")
                            }
                        )
                    } else {
                        var selectedTab by remember { mutableStateOf(0) }
                        val tabs = listOf(
                            NavigationTabItem("Tendencias", Icons.Default.Home, "tab_trends"),
                            NavigationTabItem("Auto Clics", Icons.Default.PlayArrow, "tab_autoclick"),
                            NavigationTabItem("Bonos VIP", Icons.Default.Star, "tab_bonuses"),
                            NavigationTabItem("Pensador IA", Icons.Default.ThumbUp, "tab_ai"),
                            NavigationTabItem("Stealth", Icons.Default.Settings, "tab_settings")
                        )

                        Scaffold(
                            modifier = Modifier.fillMaxSize().background(AbyssBlack),
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Column {
                                            Text(
                                                "CASINO ANALYZER",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = CyberGreen
                                            )
                                            Text(
                                                "SISTEMA REGULADOR & DETECTOR SIGILOSO",
                                                fontSize = 10.sp,
                                                color = TextGray,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    },
                                    actions = {
                                        // Quick discrete camouflage switch on the header bar
                                        IconButton(
                                            onClick = { viewModel.setCamouflageActive(true) },
                                            modifier = Modifier.testTag("header_stealth_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Lock Camouflage",
                                                tint = CyberGreen
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = SlateGray,
                                        titleContentColor = Color.White
                                    )
                                )
                            },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = SlateGray,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).testTag("navigation_panel")
                                ) {
                                    tabs.forEachIndexed { index, item ->
                                        NavigationBarItem(
                                            selected = selectedTab == index,
                                            onClick = { selectedTab = index },
                                            icon = {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    item.label,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = AbyssBlack,
                                                selectedTextColor = CyberGreen,
                                                indicatorColor = CyberGreen,
                                                unselectedIconColor = TextGray,
                                                unselectedTextColor = TextGray
                                            ),
                                            modifier = Modifier.testTag(item.tag)
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            // Display selected screen component
                            when (selectedTab) {
                                0 -> DashboardScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                                1 -> ClickConsoleScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                                2 -> BonusTrackerScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                                3 -> AIThoughtsScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                                4 -> SettingsScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class NavigationTabItem(
    val label: String,
    val icon: ImageVector,
    val tag: String
)
