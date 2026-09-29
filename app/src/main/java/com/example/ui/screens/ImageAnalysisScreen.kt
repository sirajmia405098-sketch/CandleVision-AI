package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MarketSimulator
import com.example.data.model.MarketPattern
import com.example.ui.AnalysisUiState
import com.example.ui.MainViewModel
import com.example.ui.components.DynamicCropView
import com.example.ui.components.SignalResultCard
import com.example.ui.components.renderCandlesToBitmap
import com.example.ui.theme.CallGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ImageAnalysisScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uploadedBitmap by viewModel.uploadedBitmap.collectAsStateWithLifecycle()
    val croppedBitmap by viewModel.croppedBitmap.collectAsStateWithLifecycle()
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setUploadedImageUri(it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "চার্ট স্ক্রিনশট ও ফটো এনালাইজার",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "ব্রোকারের চার্টের স্ক্রিনশট আপলোড করুন এবং সর্বশেষ ডানপাশের ক্যান্ডেল ক্রপ করুন",
            color = ElectricCyan,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Upload Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("upload_screenshot_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("গ্যালারি / ফাইল", color = TextPrimary, fontSize = 13.sp)
            }

            // Quick Sample Chart Selector
            OutlinedButton(
                onClick = {
                    val sampleScenario = MarketSimulator.generateScenario(MarketPattern.BULLISH_HAMMER)
                    val bmp = renderCandlesToBitmap(
                        sampleScenario.first,
                        sampleScenario.second.first,
                        sampleScenario.second.second,
                        width = 800,
                        height = 500
                    )
                    viewModel.setCroppedBitmap(bmp)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("load_sample_chart_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurface)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("নমুনা ব্রোকার চার্ট", color = TextPrimary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Image Crop Area
        if (uploadedBitmap != null) {
            Text(
                text = "১. ক্যান্ডেলস্টিক অঞ্চল ক্রপ করুন (Crop Target Candlestick):",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            DynamicCropView(
                bitmap = uploadedBitmap!!,
                onCropConfirmed = { cropped ->
                    viewModel.setCroppedBitmap(cropped)
                }
            )
        }

        // Display Cropped Preview
        if (croppedBitmap != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "২. ক্রপ করা ক্যান্ডেল প্রিভিউ (Cropped Area for AI):",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .border(1.dp, CallGreen, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = croppedBitmap!!.asImageBitmap(),
                    contentDescription = "Cropped Candle Preview",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val isAnalyzing = analysisState is AnalysisUiState.Analyzing
            Button(
                onClick = { viewModel.analyzeCroppedBitmap() },
                enabled = !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("analyze_cropped_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CallGreen,
                    disabledContainerColor = DarkSurfaceVariant
                )
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        color = DarkBackground,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("AI বিশ্লেষণ চলছে...", color = DarkBackground, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ক্রপ করা চার্ট বিশ্লেষণ করুন", color = DarkBackground, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Signal Result Display
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = state.message,
                        color = Color(0xFFFF5252),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
