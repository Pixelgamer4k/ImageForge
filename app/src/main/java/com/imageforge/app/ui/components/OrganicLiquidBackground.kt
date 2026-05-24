package com.imageforge.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.imageforge.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OrganicLiquidBackground(modifier: Modifier = Modifier) {
    // Infinite transition for organic liquid drifting
    val transition = rememberInfiniteTransition(label = "LiquidAurora")

    // Slow drifting animations for different color auroras
    val drift1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Drift1"
    )

    val drift2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Drift2"
    )

    val pulseScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = SineWithOptions),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestVoid) // Darkest green layer
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Calculate drifting centers
            // Blob 1: Emerald glow drifting in a slow figure-8 top right
            val x1 = width * 0.75f + sin(drift1) * (width * 0.15f)
            val y1 = height * 0.25f + cos(drift1 * 2f) * (height * 0.1f)
            val radius1 = (width * 0.45f) * pulseScale

            // Blob 2: Lime aurora drifting bottom left
            val x2 = width * 0.2f + cos(drift2) * (width * 0.1f)
            val y2 = height * 0.75f + sin(drift2) * (height * 0.15f)
            val radius2 = (width * 0.4f) / pulseScale

            // Blob 3: Deep teal mist drifting middle right
            val x3 = width * 0.85f + cos(drift1 * 1.5f) * (width * 0.1f)
            val y3 = height * 0.65f + sin(drift2 * 0.8f) * (height * 0.12f)
            val radius3 = (width * 0.35f) * (pulseScale * 0.9f)

            // Draw Blob 1 (Emerald)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(EmeraldGlow.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(x1, y1),
                    radius = radius1
                ),
                center = Offset(x1, y1),
                radius = radius1
            )

            // Draw Blob 2 (Lime)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(LimeAurora.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(x2, y2),
                    radius = radius2
                ),
                center = Offset(x2, y2),
                radius = radius2
            )

            // Draw Blob 3 (Teal)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TealMist.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(x3, y3),
                    radius = radius3
                ),
                center = Offset(x3, y3),
                radius = radius3
            )

            // Draw Ambient Center Deep Glow (Mossy Green)
            val centerRadius = width * 0.6f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ForestPond.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(width * 0.5f, height * 0.5f),
                    radius = centerRadius
                ),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = centerRadius
            )
        }

        // Dark forest tinting and vignette overlay to blend background elegantly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, ForestVoid.copy(alpha = 0.85f)),
                        radius = 1800f
                    )
                )
        )
    }
}

// Custom Easing wrapper for natural look
private val SineWithOptions = Easing { fraction ->
    sin(fraction * Math.PI.toFloat() - Math.PI.toFloat() / 2f) * 0.5f + 0.5f
}
