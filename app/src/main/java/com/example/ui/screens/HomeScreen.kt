package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TradeOutcome
import com.example.data.model.MarketPattern
import com.example.service.FloatingWidgetService
import com.example.ui.AnalysisUiState
import com.example.ui.MainViewModel
import com.example.ui.components.CandlestickChartView
import com.example.ui.components.SignalResultCard
import com.example.ui.theme.CallGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PutRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val candles by viewModel.candles.collectAsStateWithLifecycle()
    val supportLevel by viewModel.supportLevel.collectAsStateWithLifecycle()
    val resistanceLevel by viewModel.resistanceLevel.collectAsStateWithLifecycle()
    val selectedPattern by viewModel.selectedPattern.collectAsStateWithLifecycle()
    val isLiveTicking by viewModel.isLiveTicking.collectAsStateWithLifecycle()
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val autoSpeak by viewModel.autoSpeak.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
    val isOverlayActive by viewModel.isFloatingOverlayActive.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Header & Floating HUD Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isLiveTicking) CallGreen else ElectricCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CANDLEVISION PRO",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "রিয়েল-টাইম বাইনারি ক্যান্ডেলস্টিক ভিশন AI",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick Floating HUD launcher button
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    } else {
                        val serviceIntent = Intent(context, FloatingWidgetService::class.java)
                        if (FloatingWidgetService.isRunning) {
                            context.stopService(serviceIntent)
                            viewModel.setFloatingOverlayActive(false)
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent)
                            } else {
                                context.startService(serviceIntent)
                            }
                            viewModel.setFloatingOverlayActive(true)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOverlayActive) CallGreen.copy(alpha = 0.2f) else DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isOverlayActive) CallGreen else DarkCardBorder),
                modifier = Modifier.testTag("floating_hud_toggle_button")
            ) {
                Icon(
                    Icons.Default.Layers,
                    contentDescription = null,
                    tint = if (isOverlayActive) CallGreen else ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOverlayActive) "HUD চালু" else "ফ্লোটিং HUD",
                    color = if (isOverlayActive) CallGreen else TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Candlestick Chart View
        CandlestickChartView(
            candles = candles,
            supportLevel = supportLevel,
            resistanceLevel = resistanceLevel,
            highlightLatest = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Chart Controls (Live Ticking, Pattern Switcher)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Price Tick Toggle
            OutlinedButton(
                onClick = { viewModel.toggleLiveTicking() },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isLiveTicking) CallGreen.copy(alpha = 0.15f) else Color.Transparent
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isLiveTicking) CallGreen else DarkCardBorder
                ),
                modifier = Modifier.testTag("live_tick_toggle_button")
            ) {
                Icon(
                    if (isLiveTicking) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isLiveTicking) CallGreen else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLiveTicking) "লাইভ টিকিং চালু" else "লাইভ টিক শুরু",
                    color = if (isLiveTicking) CallGreen else TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Pattern Re-generate button
            IconButton(
                onClick = { viewModel.loadPattern(selectedPattern) },
                modifier = Modifier
                    .background(DarkSurfaceVariant, CircleShape)
                    .border(1.dp, DarkCardBorder, CircleShape)
                    .size(36.dp)
                    .testTag("refresh_pattern_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pattern Preset Chips
        Text(
            text = "টেস্ট প্যাটার্ন নির্বাচন করুন (Select Test Scenario):",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MarketPattern.values().forEach { pattern ->
                val isSelected = pattern == selectedPattern
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else DarkSurfaceVariant)
                        .border(1.dp, if (isSelected) ElectricCyan else DarkCardBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.loadPattern(pattern) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("pattern_${pattern.name}")
                ) {
                    Text(
                        text = pattern.bengaliName,
                        color = if (isSelected) ElectricCyan else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Primary AI Vision Action Button
        val isAnalyzing = analysisState is AnalysisUiState.Analyzing
        Button(
            onClick = { viewModel.analyzeCurrentSimulatorChart() },
            enabled = !isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("analyze_far_right_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CallGreen,
                disabledContainerColor = DarkSurfaceVariant
            )
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    color = DarkBackground,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AI ভিশন বিশ্লেষণ চলছে (Analyzing...)",
                    color = DarkBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = DarkBackground,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সর্বশেষ ক্যান্ডেল AI বিশ্লেষণ করুন (1-Tap)",
                    color = DarkBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Analysis Result State Display
        when (val state = analysisState) {
            is AnalysisUiState.Success -> {
                SignalResultCard(
                    result = state.result,
                    isSpeaking = isSpeaking,
                    onSpeakClicked = { text -> viewModel.speakText(text) },
                    onOutcomeLogged = { outcome -> viewModel.logTradeOutcome(outcome) }
                )
            }
            is AnalysisUiState.Error -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("error_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PutRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = PutRed)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = state.message, color = PutRed, fontSize = 13.sp)
                    }
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bengali TTS Audio Engine Quick Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "বাংলা অডিও সিগন্যাল (TTS Engine)",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (autoSpeak) "অটো স্পীক চালু" else "বন্ধ",
                            color = if (autoSpeak) CallGreen else TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { viewModel.setAutoSpeak(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CallGreen,
                                checkedTrackColor = CallGreen.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("auto_speak_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "স্পীড: ${String.format("%.1fx", speechRate)}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.width(70.dp)
                    )
                    Slider(
                        value = speechRate,
                        onValueChange = { viewModel.setSpeechRate(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan
                        ),
                        modifier = Modifier.weight(1f).testTag("speech_rate_slider")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
