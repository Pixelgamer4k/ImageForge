package com.imageforge.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imageforge.app.ui.screens.HistogramData
import com.imageforge.app.ui.screens.VectorscopeData
import com.imageforge.app.ui.screens.WaveformData
import com.imageforge.app.ui.theme.*

@Composable
fun AnalysisPanel(
    activeTab: Int,
    histogramData: HistogramData,
    waveformData: WaveformData,
    vectorscopeData: VectorscopeData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .glass(cornerRadius = 24.dp, bgAlpha = 0.2f, borderAlpha = 0.35f)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        when (activeTab) {
            0 -> HistogramMonitor(histogramData)
            1 -> WaveformMonitor(waveformData)
            2 -> VectorscopeMonitor(vectorscopeData)
        }
    }
}

/**
 * High-fidelity Canvas-drawn RGB Histogram Curve
 */
@Composable
fun HistogramMonitor(data: HistogramData) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RGB COLOR CHANNELS",
                color = WhiteTranslucent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LegendPill("Red", Color(0xFFEF4444))
                LegendPill("Green", Color(0xFF10B981))
                LegendPill("Blue", Color(0xFF3B82F6))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ForestDark.copy(alpha = 0.4f))
                .border(1.dp, WhiteGlassBorder.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
        ) {
            val width = size.width
            val height = size.height
            val maxCount = data.maxValue.toFloat()

            // 1. Draw Grid Lines
            val gridColor = WhiteTranslucent.copy(alpha = 0.05f)
            drawLine(gridColor, Offset(0f, height * 0.25f), Offset(width, height * 0.25f), 1f)
            drawLine(gridColor, Offset(0f, height * 0.5f), Offset(width, height * 0.5f), 1f)
            drawLine(gridColor, Offset(0f, height * 0.75f), Offset(width, height * 0.75f), 1f)

            drawLine(gridColor, Offset(width * 0.25f, 0f), Offset(width * 0.25f, height), 1f)
            drawLine(gridColor, Offset(width * 0.5f, 0f), Offset(width * 0.5f, height), 1f)
            drawLine(gridColor, Offset(width * 0.75f, 0f), Offset(width * 0.75f, height), 1f)

            // 2. Draw Paths
            val pathR = Path()
            val pathG = Path()
            val pathB = Path()

            pathR.moveTo(0f, height)
            pathG.moveTo(0f, height)
            pathB.moveTo(0f, height)

            val step = width / 255f
            for (i in 0..255) {
                val cx = i * step
                
                val valR = height - (data.redChannel[i] / maxCount) * height
                val valG = height - (data.greenChannel[i] / maxCount) * height
                val valB = height - (data.blueChannel[i] / maxCount) * height

                pathR.lineTo(cx, valR)
                pathG.lineTo(cx, valG)
                pathB.lineTo(cx, valB)
            }

            pathR.lineTo(width, height)
            pathG.lineTo(width, height)
            pathB.lineTo(width, height)

            // Draw filled curves with rich alpha layers
            drawPath(pathR, color = Color(0xFFEF4444).copy(alpha = 0.2f))
            drawPath(pathR, color = Color(0xFFEF4444), style = Stroke(width = 1.5.dp.toPx()))

            drawPath(pathG, color = Color(0xFF10B981).copy(alpha = 0.2f))
            drawPath(pathG, color = Color(0xFF10B981), style = Stroke(width = 1.5.dp.toPx()))

            drawPath(pathB, color = Color(0xFF3B82F6).copy(alpha = 0.2f))
            drawPath(pathB, color = Color(0xFF3B82F6), style = Stroke(width = 1.5.dp.toPx()))
        }
    }
}

/**
 * Dynamic oscilloscope phosphorescent luminance Waveform Monitor
 */
@Composable
fun WaveformMonitor(data: WaveformData) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LUMINANCE WAVEFORM (IRE)",
                color = WhiteTranslucent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "SCOPE: 0 - 100 IRE",
                color = LimeAurora,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ForestVoid.copy(alpha = 0.6f))
                .border(1.dp, WhiteGlassBorder.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw standard broadcast IRE line marks
            val gridColor = WhiteTranslucent.copy(alpha = 0.08f)
            drawLine(gridColor, Offset(0f, height * 0f), Offset(width, height * 0f), 1f) // 100 IRE
            drawLine(gridColor, Offset(0f, height * 0.2f), Offset(width, height * 0.2f), 1f) // 80 IRE
            drawLine(gridColor, Offset(0f, height * 0.4f), Offset(width, height * 0.4f), 1f) // 60 IRE
            drawLine(gridColor, Offset(0f, height * 0.6f), Offset(width, height * 0.6f), 1f) // 40 IRE
            drawLine(gridColor, Offset(0f, height * 0.8f), Offset(width, height * 0.8f), 1f) // 20 IRE
            drawLine(gridColor, Offset(0f, height * 1f), Offset(width, height * 1f), 1f) // 0 IRE

            // 2. Draw Column point values mimicking phosphors glow
            val cols = data.luminanceBins.size
            val rows = data.luminanceBins[0].size
            val maxCount = data.maxValue.toFloat()

            val colStep = width / cols
            val rowStep = height / rows

            for (c in 0 until cols) {
                val cx = c * colStep + colStep * 0.5f
                for (r in 0 until rows) {
                    val count = data.luminanceBins[c][r]
                    if (count > 0) {
                        val cy = height - (r * rowStep)
                        val intensity = (count / maxCount).coerceIn(0.1f, 1.0f)
                        
                        // Draw glow dot
                        drawCircle(
                            color = LimeAurora.copy(alpha = intensity * 0.85f),
                            radius = 1.8.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Standard Chrominance Polar Vectorscope Monitor
 */
@Composable
fun VectorscopeMonitor(data: VectorscopeData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CHROMINANCE VECTORSCOPE",
            color = WhiteTranslucent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Canvas(
            modifier = Modifier
                .size(170.dp)
                .clip(RoundedCornerShape(85.dp))
                .background(ForestDark.copy(alpha = 0.4f))
                .border(1.dp, WhiteGlassBorder.copy(alpha = 0.1f), RoundedCornerShape(85.dp))
        ) {
            val center = Offset(size.width * 0.5f, size.height * 0.5f)
            val radius = size.width * 0.5f

            // 1. Draw polar concentric graticule rings
            drawCircle(
                color = WhiteTranslucent.copy(alpha = 0.05f),
                radius = radius * 0.35f,
                center = center,
                style = Stroke(1f)
            )
            drawCircle(
                color = WhiteTranslucent.copy(alpha = 0.05f),
                radius = radius * 0.70f,
                center = center,
                style = Stroke(1f)
            )
            drawCircle(
                color = LimeAurora.copy(alpha = 0.15f),
                radius = radius * 0.95f,
                center = center,
                style = Stroke(1.5f)
            )

            // 2. Crosshair grid lines
            drawLine(WhiteTranslucent.copy(alpha = 0.05f), Offset(center.x, 0f), Offset(center.x, size.height), 1f)
            drawLine(WhiteTranslucent.copy(alpha = 0.05f), Offset(0f, center.y), Offset(size.width, center.y), 1f)

            // 3. Targets for primary colors (R, Y, G, C, B, M)
            // standard vectorscope coordinates (angle offset relative to Center)
            val colorLabels = listOf(
                Triple("R", -60f, Color(0xFFEF4444)),
                Triple("Y", -120f, Color(0xFFFBBF24)),
                Triple("G", -180f, Color(0xFF10B981)),
                Triple("C", 120f, Color(0xFF22D3EE)),
                Triple("B", 60f, Color(0xFF3B82F6)),
                Triple("M", 0f, Color(0xFFEC4899))
            )

            colorLabels.forEach { (label, angle, col) ->
                val rad = Math.toRadians(angle.toDouble())
                val targetRadius = radius * 0.78f
                val tx = center.x + Math.cos(rad).toFloat() * targetRadius
                val ty = center.y + Math.sin(rad).toFloat() * targetRadius

                // Small Target box
                drawRect(
                    color = col.copy(alpha = 0.3f),
                    topLeft = Offset(tx - 6f, ty - 6f),
                    size = androidx.compose.ui.geometry.Size(12f, 12f)
                )
                drawRect(
                    color = col,
                    topLeft = Offset(tx - 6f, ty - 6f),
                    size = androidx.compose.ui.geometry.Size(12f, 12f),
                    style = Stroke(1f)
                )
            }

            // 4. Plot saturated chromatic points
            data.points.forEach { pt ->
                val px = center.x + pt.x
                val py = center.y + pt.y
                
                // Draw glow chromatic particles
                drawCircle(
                    color = TealMist.copy(alpha = 0.8f),
                    radius = 2f,
                    center = Offset(px, py)
                )
            }
        }
    }
}

@Composable
fun LegendPill(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(label, color = WhiteTranslucent, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}
