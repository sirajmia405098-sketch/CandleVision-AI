package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TradeOutcome
import com.example.data.model.AnalysisResult
import com.example.data.model.ConfidenceLevel
import com.example.data.model.SignalType
import com.example.ui.theme.AvoidAmber
import com.example.ui.theme.AvoidAmberGlow
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallGreenGlow
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PutRed
import com.example.ui.theme.PutRedGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SignalResultCard(
    result: AnalysisResult,
    isSpeaking: Boolean,
    onSpeakClicked: (String) -> Unit,
    onOutcomeLogged: (TradeOutcome) -> Unit,
    modifier: Modifier = Modifier
) {
    val signal = result.signal
    val signalColor = when (signal) {
        SignalType.CALL -> CallGreen
        SignalType.PUT -> PutRed
        SignalType.AVOID -> AvoidAmber
    }
    val signalGlow = when (signal) {
        SignalType.CALL -> CallGreenGlow
        SignalType.PUT -> PutRedGlow
        SignalType.AVOID -> AvoidAmberGlow
    }

    // 60-second binary expiry timer state
    var isTimerActive by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(60) }

    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            secondsLeft = 60
            while (secondsLeft > 0 && isTimerActive) {
                delay(1000L)
                secondsLeft--
            }
            if (secondsLeft == 0) {
                isTimerActive = false
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseWave")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("signal_result_card")
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, signalColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
        color = DarkSurface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Signal Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(signalGlow, CircleShape)
                            .border(2.dp, signalColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        when (signal) {
                            SignalType.CALL -> Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = "CALL UP",
                                tint = CallGreen,
                                modifier = Modifier.size(32.dp)
                            )
                            SignalType.PUT -> Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "PUT DOWN",
                                tint = PutRed,
                                modifier = Modifier.size(32.dp)
                            )
                            SignalType.AVOID -> Icon(
                                Icons.Default.Block,
                                contentDescription = "AVOID WAIT",
                                tint = AvoidAmber,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = signal.label,
                                color = signalColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(signalColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = signal.bengaliLabel,
                                    color = signalColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = signal.action,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // TTS Speaker Button
                IconButton(
                    onClick = { onSpeakClicked(result.rawBengaliText) },
                    modifier = Modifier
                        .background(if (isSpeaking) ElectricCyan.copy(alpha = 0.2f) else DarkSurfaceVariant, CircleShape)
                        .border(1.dp, if (isSpeaking) ElectricCyan else DarkCardBorder, CircleShape)
                        .testTag("speaker_button")
                ) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Speak Signal Bengali",
                        tint = if (isSpeaking) ElectricCyan else TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bengali Output Box (Mandatory 1-2 short sentences)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF090D16))
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI সিগন্যাল বার্তা (Bengali AI Output)",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (result.latencyMs > 0) {
                            Text(
                                text = "${result.latencyMs}ms",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result.rawBengaliText,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 23.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Key Technical Metrics Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Confidence badge
                val confProgress = when (result.confidence) {
                    ConfidenceLevel.HIGH -> 0.95f
                    ConfidenceLevel.MEDIUM -> 0.65f
                    ConfidenceLevel.LOW -> 0.35f
                }
                val confColor = when (result.confidence) {
                    ConfidenceLevel.HIGH -> CallGreen
                    ConfidenceLevel.MEDIUM -> AvoidAmber
                    ConfidenceLevel.LOW -> PutRed
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("কনফিডেন্স", color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            result.confidence.bengaliLabel,
                            color = confColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { confProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = confColor,
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }

                // Pattern badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("প্যাটার্ন", color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            result.patternDetected,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            result.trend,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Binary Expiration Timer & Trade Logging
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 60s Binary Expiry Countdown
                OutlinedButton(
                    onClick = {
                        isTimerActive = !isTimerActive
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isTimerActive) signalColor.copy(alpha = 0.15f) else Color.Transparent
                    ),
                    modifier = Modifier.testTag("binary_timer_button")
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (isTimerActive) signalColor else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTimerActive) "এক্সপায়ারি: ${secondsLeft}s" else "১-মিনিট টাইমার শুরু",
                        color = if (isTimerActive) signalColor else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quick Win / Loss / Skip Logger
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .background(CallGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, CallGreen, RoundedCornerShape(8.dp))
                            .clickable { onOutcomeLogged(TradeOutcome.WIN) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("log_win_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("WIN ✓", color = CallGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .background(PutRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, PutRed, RoundedCornerShape(8.dp))
                            .clickable { onOutcomeLogged(TradeOutcome.LOSS) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("log_loss_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("LOSS ✗", color = PutRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
