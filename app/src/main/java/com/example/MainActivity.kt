package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CandlestickChart
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ActiveTab
import com.example.ui.MainViewModel
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImageAnalysisScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CallGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()

                // BackHandler to return to simulator if on other tabs
                if (activeTab != ActiveTab.SIMULATOR) {
                    BackHandler {
                        viewModel.setActiveTab(ActiveTab.SIMULATOR)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkBackground,
                    bottomBar = {
                        CandleVisionBottomBar(
                            activeTab = activeTab,
                            onTabSelected = { viewModel.setActiveTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            ActiveTab.SIMULATOR -> HomeScreen(viewModel = viewModel)
                            ActiveTab.IMAGE_CROPPER -> ImageAnalysisScreen(viewModel = viewModel)
                            ActiveTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                            ActiveTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CandleVisionBottomBar(
    activeTab: ActiveTab,
    onTabSelected: (ActiveTab) -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .background(DarkSurface)
            .testTag("candlevision_bottom_bar")
    ) {
        NavigationBarItem(
            selected = activeTab == ActiveTab.SIMULATOR,
            onClick = { onTabSelected(ActiveTab.SIMULATOR) },
            icon = {
                Icon(
                    if (activeTab == ActiveTab.SIMULATOR) Icons.Filled.CandlestickChart else Icons.Outlined.CandlestickChart,
                    contentDescription = "লাইভ চার্ট",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "লাইভ চার্ট",
                    fontSize = 11.sp,
                    fontWeight = if (activeTab == ActiveTab.SIMULATOR) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CallGreen,
                selectedTextColor = CallGreen,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = CallGreen.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("tab_simulator")
        )

        NavigationBarItem(
            selected = activeTab == ActiveTab.IMAGE_CROPPER,
            onClick = { onTabSelected(ActiveTab.IMAGE_CROPPER) },
            icon = {
                Icon(
                    if (activeTab == ActiveTab.IMAGE_CROPPER) Icons.Filled.Crop else Icons.Outlined.Crop,
                    contentDescription = "ক্রপ ও ফটো",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "ক্রপ ও ফটো",
                    fontSize = 11.sp,
                    fontWeight = if (activeTab == ActiveTab.IMAGE_CROPPER) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricCyan,
                selectedTextColor = ElectricCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = ElectricCyan.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("tab_cropper")
        )

        NavigationBarItem(
            selected = activeTab == ActiveTab.HISTORY,
            onClick = { onTabSelected(ActiveTab.HISTORY) },
            icon = {
                Icon(
                    if (activeTab == ActiveTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                    contentDescription = "হিস্ট্রি",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "হিস্ট্রি",
                    fontSize = 11.sp,
                    fontWeight = if (activeTab == ActiveTab.HISTORY) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricCyan,
                selectedTextColor = ElectricCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = ElectricCyan.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("tab_history")
        )

        NavigationBarItem(
            selected = activeTab == ActiveTab.SETTINGS,
            onClick = { onTabSelected(ActiveTab.SETTINGS) },
            icon = {
                Icon(
                    if (activeTab == ActiveTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "সেটিংস",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "সেটিংস",
                    fontSize = 11.sp,
                    fontWeight = if (activeTab == ActiveTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricCyan,
                selectedTextColor = ElectricCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = ElectricCyan.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("tab_settings")
        )
    }
}
