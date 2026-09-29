package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan

@Composable
fun DynamicCropView(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    onCropConfirmed: (Bitmap) -> Unit
) {
    // Relative coordinates [0.0..1.0] for crop rectangle
    var cropLeft by remember { mutableStateOf(0.60f) } // Default to far-right ~40% for latest candles
    var cropTop by remember { mutableStateOf(0.05f) }
    var cropRight by remember { mutableStateOf(0.98f) }
    var cropBottom by remember { mutableStateOf(0.95f) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val w = size.width
                            val h = size.height
                            val dx = dragAmount.x / w
                            val dy = dragAmount.y / h

                            // Drag box or adjust
                            val widthRatio = cropRight - cropLeft
                            val heightRatio = cropBottom - cropTop
                            val newLeft = (cropLeft + dx).coerceIn(0f, 1f - widthRatio)
                            val newTop = (cropTop + dy).coerceIn(0f, 1f - heightRatio)
                            cropLeft = newLeft
                            cropRight = newLeft + widthRatio
                            cropTop = newTop
                            cropBottom = newTop + heightRatio
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height

                // Draw background bitmap scaled to fit
                val srcRect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
                val dstRect = android.graphics.Rect(0, 0, canvasW.toInt(), canvasH.toInt())
                drawContext.canvas.nativeCanvas.drawBitmap(bitmap, srcRect, dstRect, null)

                // Dim areas outside crop box
                val cX = cropLeft * canvasW
                val cY = cropTop * canvasH
                val cW = (cropRight - cropLeft) * canvasW
                val cH = (cropBottom - cropTop) * canvasH

                // Top dim
                drawRect(Color.Black.copy(alpha = 0.55f), Offset(0f, 0f), Size(canvasW, cY))
                // Bottom dim
                drawRect(Color.Black.copy(alpha = 0.55f), Offset(0f, cY + cH), Size(canvasW, canvasH - (cY + cH)))
                // Left dim
                drawRect(Color.Black.copy(alpha = 0.55f), Offset(0f, cY), Size(cX, cH))
                // Right dim
                drawRect(Color.Black.copy(alpha = 0.55f), Offset(cX + cW, cY), Size(canvasW - (cX + cW), cH))

                // Highlight border
                drawRect(
                    color = ElectricCyan,
                    topLeft = Offset(cX, cY),
                    size = Size(cW, cH),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Corner indicator marks
                val cornerLen = 24.dp.toPx()
                drawLine(ElectricCyan, Offset(cX, cY), Offset(cX + cornerLen, cY), 6f)
                drawLine(ElectricCyan, Offset(cX, cY), Offset(cX, cY + cornerLen), 6f)

                drawLine(ElectricCyan, Offset(cX + cW, cY), Offset(cX + cW - cornerLen, cY), 6f)
                drawLine(ElectricCyan, Offset(cX + cW, cY), Offset(cX + cW, cY + cornerLen), 6f)

                drawLine(ElectricCyan, Offset(cX, cY + cH), Offset(cX, cY + cH - cornerLen), 6f)
                drawLine(ElectricCyan, Offset(cX, cY + cH), Offset(cX + cornerLen, cY + cH), 6f)

                drawLine(ElectricCyan, Offset(cX + cW, cY + cH), Offset(cX + cW - cornerLen, cY + cH), 6f)
                drawLine(ElectricCyan, Offset(cX + cW, cY + cH), Offset(cX + cW, cY + cH - cornerLen), 6f)
            }

            // Target banner
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color(0xFF090D16).copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "লক্ষ্য: সর্বশেষ ডানপাশের ক্যান্ডেলস্টিক (Far-Right)",
                    color = ElectricCyan,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Preset Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    cropLeft = 0.60f
                    cropTop = 0.05f
                    cropRight = 0.98f
                    cropBottom = 0.95f
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (cropLeft == 0.60f) Color(0xFF1E293B) else Color.Transparent
                )
            ) {
                Icon(Icons.Default.ViewSidebar, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ডানপাশ (Latest)", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    cropLeft = 0.02f
                    cropTop = 0.02f
                    cropRight = 0.98f
                    cropBottom = 0.98f
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (cropLeft == 0.02f) Color(0xFF1E293B) else Color.Transparent
                )
            ) {
                Icon(Icons.Default.CropFree, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("সম্পূর্ণ চার্ট", fontSize = 11.sp)
            }

            androidx.compose.material3.Button(
                onClick = {
                    val cropBmp = cropBitmap(bitmap, cropLeft, cropTop, cropRight, cropBottom)
                    onCropConfirmed(cropBmp)
                },
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Icon(Icons.Default.Crop, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ক্রপ নিশ্চিত", color = Color.Black, fontSize = 12.sp)
            }
        }
    }
}

fun cropBitmap(source: Bitmap, leftRatio: Float, topRatio: Float, rightRatio: Float, bottomRatio: Float): Bitmap {
    val srcW = source.width
    val srcH = source.height
    val x = (leftRatio * srcW).toInt().coerceIn(0, srcW - 1)
    val y = (topRatio * srcH).toInt().coerceIn(0, srcH - 1)
    val w = ((rightRatio - leftRatio) * srcW).toInt().coerceIn(1, srcW - x)
    val h = ((bottomRatio - topRatio) * srcH).toInt().coerceIn(1, srcH - y)
    return Bitmap.createBitmap(source, x, y, w, h)
}
