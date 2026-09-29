package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TradeHistoryEntity
import com.example.data.local.TradeOutcome
import com.example.ui.MainViewModel
import com.example.ui.theme.AvoidAmber
import com.example.ui.theme.CallGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PutRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val trades by viewModel.allTrades.collectAsStateWithLifecycle()
    val totalTrades by viewModel.totalTradesCount.collectAsStateWithLifecycle()
    val winTrades by viewModel.winTradesCount.collectAsStateWithLifecycle()
    val lossTrades by viewModel.lossTradesCount.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }

    val completedTrades = winTrades + lossTrades
    val winRate = if (completedTrades > 0) (winTrades.toFloat() / completedTrades * 100).toInt() else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ট্রেডিং সিগন্যাল হিস্ট্রি ও উইন-রেট",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "সংরক্ষিত সিগন্যাল এবং ফলাফল ট্র্যাকিং",
                    color = ElectricCyan,
                    fontSize = 12.sp
                )
            }

            if (trades.isNotEmpty()) {
                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Performance Stat Cards Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("উইন রেট (Win Rate)", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "$winRate%",
                            color = if (winRate >= 70) CallGreen else if (winRate >= 50) AvoidAmber else ElectricCyan,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মোট", color = TextSecondary, fontSize = 10.sp)
                            Text("$totalTrades", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WIN", color = CallGreen, fontSize = 10.sp)
                            Text("$winTrades", color = CallGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LOSS", color = PutRed, fontSize = 10.sp)
                            Text("$lossTrades", color = PutRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (winRate / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CallGreen,
                    trackColor = Color(0xFF1E293B)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Trades List
        if (trades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Insights,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "এখনো কোন সিগন্যাল হিস্ট্রি নেই",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "হোম স্ক্রিন থেকে ক্যান্ডেলস্টিক চার্ট এনালাইজ করুন",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("trades_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(trades, key = { it.id }) { trade ->
                    TradeHistoryItem(
                        trade = trade,
                        onSpeak = { viewModel.speakText(trade.bengaliAnalysis) },
                        onOutcomeChanged = { outcome -> viewModel.updateTradeOutcome(trade.id, outcome) },
                        onDelete = { viewModel.deleteTrade(trade.id) }
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("হিস্ট্রি মুছে ফেলুন?", color = TextPrimary) },
            text = { Text("আপনি কি সমস্ত সংরক্ষিত সিগন্যাল মুছে ফেলতে চান?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearTradeHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("হ্যাঁ, মুছুন", color = PutRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("বাতিল", color = TextPrimary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun TradeHistoryItem(
    trade: TradeHistoryEntity,
    onSpeak: () -> Unit,
    onOutcomeChanged: (TradeOutcome) -> Unit,
    onDelete: () -> Unit
) {
    val signalColor = when (trade.signal) {
        "CALL" -> CallGreen
        "PUT" -> PutRed
        else -> AvoidAmber
    }

    val outcome = try {
        TradeOutcome.valueOf(trade.outcome)
    } catch (_: Exception) {
        TradeOutcome.PENDING
    }

    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(trade.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(signalColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, signalColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = trade.signal,
                            color = signalColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = trade.pattern,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateStr,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    IconButton(onClick = onSpeak, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = trade.bengaliAnalysis,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Outcome selector (WIN / LOSS / TIE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ফলাফল: ${outcome.name}",
                    color = when (outcome) {
                        TradeOutcome.WIN -> CallGreen
                        TradeOutcome.LOSS -> PutRed
                        else -> TextSecondary
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (outcome == TradeOutcome.WIN) CallGreen else DarkSurfaceVariant)
                            .clickable { onOutcomeChanged(TradeOutcome.WIN) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "WIN",
                            color = if (outcome == TradeOutcome.WIN) Color.Black else TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (outcome == TradeOutcome.LOSS) PutRed else DarkSurfaceVariant)
                            .clickable { onOutcomeChanged(TradeOutcome.LOSS) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "LOSS",
                            color = if (outcome == TradeOutcome.LOSS) Color.White else TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (outcome == TradeOutcome.TIE) AvoidAmber else DarkSurfaceVariant)
                            .clickable { onOutcomeChanged(TradeOutcome.TIE) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "TIE",
                            color = if (outcome == TradeOutcome.TIE) Color.Black else TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
