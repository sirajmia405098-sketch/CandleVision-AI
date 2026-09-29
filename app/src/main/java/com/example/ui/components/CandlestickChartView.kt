package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candlestick
import com.example.ui.theme.CandleGreen
import com.example.ui.theme.CandleRed
import com.example.ui.theme.CandleWick
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GridLine
import com.example.ui.theme.ResistanceLine
import com.example.ui.theme.SupportLine

@Composable
fun CandlestickChartView(
    candles: List<Candlestick>,
    modifier: Modifier = Modifier,
    supportLevel: Float? = null,
    resistanceLevel: Float? = null,
    highlightLatest: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (candles.isEmpty()) return@Canvas

            val w = size.width
            val h = size.height
            val paddingBottom = 24f
            val chartHeight = h - paddingBottom
            val chartWidth = w - 60f // reserve space for price axis

            var minPrice = candles.minOf { it.low }
            var maxPrice = candles.maxOf { it.high }
            if (supportLevel != null) minPrice = minOf(minPrice, supportLevel)
            if (resistanceLevel != null) maxPrice = maxOf(maxPrice, resistanceLevel)

            val priceRange = if (maxPrice - minPrice == 0f) 1f else maxPrice - minPrice
            val priceMargin = priceRange * 0.08f
            val effectiveMin = minPrice - priceMargin
            val effectiveMax = maxPrice + priceMargin
            val effectiveRange = effectiveMax - effectiveMin

            fun priceToY(price: Float): Float {
                val ratio = (price - effectiveMin) / effectiveRange
                return chartHeight - (ratio * chartHeight)
            }

            // Draw horizontal grid lines & prices
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val p = effectiveMin + (effectiveRange * (i.toFloat() / gridSteps))
                val y = priceToY(p)
                drawLine(
                    color = GridLine,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f
                )

                // Price label text on right axis
                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%.4f", p),
                    chartWidth + 6f,
                    y + 4f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#94A3B8")
                        textSize = 24f
                        isAntiAlias = true
                    }
                )
            }

            // Draw Support Line
            if (supportLevel != null) {
                val sY = priceToY(supportLevel)
                drawLine(
                    color = SupportLine,
                    start = Offset(0f, sY),
                    end = Offset(chartWidth, sY),
                    strokeWidth = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "SUPPORT (সাপোর্ট)",
                    12f,
                    sY - 6f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#00E5FF")
                        textSize = 22f
                        isFakeBoldText = true
                    }
                )
            }

            // Draw Resistance Line
            if (resistanceLevel != null) {
                val rY = priceToY(resistanceLevel)
                drawLine(
                    color = ResistanceLine,
                    start = Offset(0f, rY),
                    end = Offset(chartWidth, rY),
                    strokeWidth = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "RESISTANCE (রেজিস্ট্যান্স)",
                    12f,
                    rY + 22f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#FF5252")
                        textSize = 22f
                        isFakeBoldText = true
                    }
                )
            }

            // Draw Candlesticks
            val n = candles.size
            val candleSpacing = chartWidth / n.coerceAtLeast(1)
            val candleBodyWidth = (candleSpacing * 0.65f).coerceIn(4f, 24f)

            candles.forEachIndexed { i, candle ->
                val centerX = (i + 0.5f) * candleSpacing
                val highY = priceToY(candle.high)
                val lowY = priceToY(candle.low)
                val openY = priceToY(candle.open)
                val closeY = priceToY(candle.close)

                val candleColor = if (candle.isBullish) CandleGreen else CandleRed

                // Draw Wick
                drawLine(
                    color = CandleWick,
                    start = Offset(centerX, highY),
                    end = Offset(centerX, lowY),
                    strokeWidth = 2f
                )

                // Draw Body
                val bodyTop = minOf(openY, closeY)
                val bodyHeight = (kotlin.math.abs(openY - closeY)).coerceAtLeast(2f)
                val left = centerX - (candleBodyWidth / 2f)

                drawRect(
                    color = candleColor,
                    topLeft = Offset(left, bodyTop),
                    size = Size(candleBodyWidth, bodyHeight)
                )

                // Highlight latest far-right candle (The target of AI vision analysis)
                if (highlightLatest && i == candles.size - 1) {
                    val highlightBoxLeft = centerX - (candleBodyWidth * 1.3f)
                    val highlightBoxWidth = candleBodyWidth * 2.6f
                    val highlightTop = highY - 14f
                    val highlightHeight = (lowY - highY) + 28f

                    drawRoundRect(
                        color = Color(0xFF00E5FF).copy(alpha = pulseAlpha * 0.25f),
                        topLeft = Offset(highlightBoxLeft, highlightTop),
                        size = Size(highlightBoxWidth, highlightHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )

                    drawRoundRect(
                        color = Color(0xFF00E5FF).copy(alpha = pulseAlpha),
                        topLeft = Offset(highlightBoxLeft, highlightTop),
                        size = Size(highlightBoxWidth, highlightHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                        style = Stroke(width = 2.5f)
                    )

                    // Target AI pointer indicator
                    drawContext.canvas.nativeCanvas.drawText(
                        "AI TARGET",
                        centerX - 35f,
                        highlightTop - 6f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#00E5FF")
                            textSize = 20f
                            isFakeBoldText = true
                        }
                    )
                }
            }
        }

        // Live badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color(0xFF090D16).copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF2E3B52), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "EUR/USD 1M • BINARY LIVE",
                color = Color(0xFF00E5FF),
                fontSize = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}

/**
 * Utility to generate high resolution Bitmap from candles for Gemini AI vision processing.
 */
fun renderCandlesToBitmap(
    candles: List<Candlestick>,
    supportLevel: Float? = null,
    resistanceLevel: Float? = null,
    width: Int = 800,
    height: Int = 450
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Dark background
    canvas.drawColor(android.graphics.Color.parseColor("#090D16"))

    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    // Calculate scaling
    var minPrice = candles.minOfOrNull { it.low } ?: 1.0f
    var maxPrice = candles.maxOfOrNull { it.high } ?: 1.1f
    if (supportLevel != null) minPrice = minOf(minPrice, supportLevel)
    if (resistanceLevel != null) maxPrice = maxOf(maxPrice, resistanceLevel)

    val range = if (maxPrice - minPrice == 0f) 1f else maxPrice - minPrice
    val margin = range * 0.1f
    val effMin = minPrice - margin
    val effMax = maxPrice + margin
    val effRange = effMax - effMin

    fun toY(price: Float): Float {
        val ratio = (price - effMin) / effRange
        return (height - 40) - (ratio * (height - 80))
    }

    // Grid lines
    paint.color = android.graphics.Color.parseColor("#1A2333")
    paint.strokeWidth = 1.5f
    for (i in 0..4) {
        val y = toY(effMin + (effRange * (i / 4f)))
        canvas.drawLine(0f, y, width.toFloat(), y, paint)
    }

    // Support Line
    if (supportLevel != null) {
        paint.color = android.graphics.Color.parseColor("#00E5FF")
        paint.strokeWidth = 3f
        val sY = toY(supportLevel)
        canvas.drawLine(0f, sY, width.toFloat(), sY, paint)
    }

    // Resistance Line
    if (resistanceLevel != null) {
        paint.color = android.graphics.Color.parseColor("#FF5252")
        paint.strokeWidth = 3f
        val rY = toY(resistanceLevel)
        canvas.drawLine(0f, rY, width.toFloat(), rY, paint)
    }

    // Draw candles
    val n = candles.size
    val spacing = width.toFloat() / n.coerceAtLeast(1)
    val bodyW = (spacing * 0.7f).coerceIn(6f, 32f)

    candles.forEachIndexed { i, c ->
        val cx = (i + 0.5f) * spacing
        val hY = toY(c.high)
        val lY = toY(c.low)
        val oY = toY(c.open)
        val cY = toY(c.close)

        // Wick
        paint.color = android.graphics.Color.parseColor("#90A4AE")
        paint.strokeWidth = 2.5f
        canvas.drawLine(cx, hY, cx, lY, paint)

        // Body
        val isBull = c.isBullish
        paint.color = if (isBull) android.graphics.Color.parseColor("#00C853") else android.graphics.Color.parseColor("#D50000")
        val top = minOf(oY, cY)
        val bH = kotlin.math.abs(oY - cY).coerceAtLeast(3f)
        canvas.drawRect(cx - (bodyW / 2), top, cx + (bodyW / 2), top + bH, paint)
    }

    return bitmap
}
