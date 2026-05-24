package com.imageforge.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imageforge.app.ui.screens.ComparisonMode
import com.imageforge.app.ui.theme.*

/**
 * Custom rectangular shape for pixel-aligned split screen peeking
 */
class SplitClipShape(private val splitRatio: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = 0f,
                right = size.width * splitRatio,
                bottom = size.height
            )
        )
    }
}

@Composable
fun ComparisonArea(
    mode: ComparisonMode,
    imageA: Bitmap,
    imageB: Bitmap,
    overlayRatio: Float,
    swipeRatio: Float,
    zoomPercent: Float,
    panOffset: Offset,
    isSynced: Boolean,
    onRatioChanged: (Float) -> Unit,
    onSwipeChanged: (Float) -> Unit,
    onZoomPanChanged: (Float, Offset) -> Unit,
    onLoadImageA: () -> Unit,
    onLoadImageB: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .glass(cornerRadius = 24.dp, bgAlpha = 0.15f, borderAlpha = 0.25f)
            .padding(8.dp)
    ) {
        when (mode) {
            ComparisonMode.SIDE_BY_SIDE -> {
                SideBySideView(
                    imageA = imageA,
                    imageB = imageB,
                    zoomPercent = zoomPercent,
                    panOffset = panOffset,
                    isSynced = isSynced,
                    onZoomPanChanged = onZoomPanChanged
                )
            }
            ComparisonMode.OVERLAY -> {
                OverlayView(
                    imageA = imageA,
                    imageB = imageB,
                    overlayRatio = overlayRatio,
                    zoomPercent = zoomPercent,
                    panOffset = panOffset,
                    onZoomPanChanged = onZoomPanChanged,
                    onRatioChanged = onRatioChanged
                )
            }
            ComparisonMode.SWIPE -> {
                SwipeView(
                    imageA = imageA,
                    imageB = imageB,
                    swipeRatio = swipeRatio,
                    zoomPercent = zoomPercent,
                    panOffset = panOffset,
                    onZoomPanChanged = onZoomPanChanged,
                    onSwipeChanged = onSwipeChanged
                )
            }
        }

        // Floating top-right LOAD A & LOAD B buttons
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .glass(cornerRadius = 12.dp, bgAlpha = 0.5f, borderAlpha = 0.6f)
                    .clickable { onLoadImageA() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Load Image A",
                        tint = LimeAurora,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("LOAD A", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .glass(cornerRadius = 12.dp, bgAlpha = 0.5f, borderAlpha = 0.6f)
                    .clickable { onLoadImageB() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Load Image B",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("LOAD B", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Floating bottom-left Zoom Slider control card
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .width(180.dp)
                .glass(cornerRadius = 16.dp, bgAlpha = 0.5f, borderAlpha = 0.5f)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom slider icon",
                    tint = WhiteTranslucent,
                    modifier = Modifier.size(16.dp)
                )
                Slider(
                    value = zoomPercent,
                    onValueChange = { onZoomPanChanged(it, panOffset) },
                    valueRange = 100f..1000f,
                    colors = SliderDefaults.colors(
                        thumbColor = LimeAurora,
                        activeTrackColor = EmeraldGlow,
                        inactiveTrackColor = ForestPond
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${zoomPercent.toInt()}%",
                    color = LimeAurora,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SideBySideView(
    imageA: Bitmap,
    imageB: Bitmap,
    zoomPercent: Float,
    panOffset: Offset,
    isSynced: Boolean,
    onZoomPanChanged: (Float, Offset) -> Unit
) {
    // Unique zoom/pan configurations for non-synced comparisons
    var localZoomB by remember { mutableStateOf(100f) }
    var localPanB by remember { mutableStateOf(Offset.Zero) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Image A Column
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                .background(ForestDark.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newZoom = (zoomPercent * zoom).coerceIn(100f, 1000f)
                        val newPan = panOffset + pan
                        onZoomPanChanged(newZoom, newPan)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = imageA.asImageBitmap(),
                contentDescription = "Image A Viewport",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = zoomPercent / 100f,
                        scaleY = zoomPercent / 100f,
                        translationX = panOffset.x,
                        translationY = panOffset.y
                    )
            )

            // Title Label Card
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .glass(cornerRadius = 10.dp, bgAlpha = 0.5f, borderAlpha = 0.5f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("IMAGE A (Summer)", color = LimeAurora, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Image B Column
        val zoomB = if (isSynced) zoomPercent else localZoomB
        val panB = if (isSynced) panOffset else localPanB

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                .background(ForestDark.copy(alpha = 0.5f))
                .pointerInput(isSynced) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val targetZoom = (zoomB * zoom).coerceIn(100f, 1000f)
                        val targetPan = panB + pan
                        if (isSynced) {
                            onZoomPanChanged(targetZoom, targetPan)
                        } else {
                            localZoomB = targetZoom
                            localPanB = targetPan
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = imageB.asImageBitmap(),
                contentDescription = "Image B Viewport",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = zoomB / 100f,
                        scaleY = zoomB / 100f,
                        translationX = panB.x,
                        translationY = panB.y
                    )
            )

            // Title Label Card
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .glass(cornerRadius = 10.dp, bgAlpha = 0.5f, borderAlpha = 0.5f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("IMAGE B (Autumn/Filter)", color = EmeraldGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OverlayView(
    imageA: Bitmap,
    imageB: Bitmap,
    overlayRatio: Float,
    zoomPercent: Float,
    panOffset: Offset,
    onZoomPanChanged: (Float, Offset) -> Unit,
    onRatioChanged: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(ForestDark.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newZoom = (zoomPercent * zoom).coerceIn(100f, 1000f)
                    val newPan = panOffset + pan
                    onZoomPanChanged(newZoom, newPan)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Underlay Base (Image A)
        Image(
            bitmap = imageA.asImageBitmap(),
            contentDescription = "Image A Underlay",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoomPercent / 100f,
                    scaleY = zoomPercent / 100f,
                    translationX = panOffset.x,
                    translationY = panOffset.y
                )
        )

        // Overlay Layer (Image B with translucent alpha)
        Image(
            bitmap = imageB.asImageBitmap(),
            contentDescription = "Image B Overlay",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoomPercent / 100f,
                    scaleY = zoomPercent / 100f,
                    translationX = panOffset.x,
                    translationY = panOffset.y,
                    alpha = overlayRatio
                )
        )

        // Top Information Pill
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp)
                .glass(cornerRadius = 12.dp, bgAlpha = 0.5f, borderAlpha = 0.5f)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "BLEND MODE: ${(overlayRatio * 100).toInt()}% B",
                color = LimeAurora,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Bottom floating blend slider card
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.85f)
                .padding(bottom = 20.dp)
                .glass(cornerRadius = 20.dp, bgAlpha = 0.6f, borderAlpha = 0.5f)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("A", color = WhiteTranslucent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = overlayRatio,
                    onValueChange = onRatioChanged,
                    colors = SliderDefaults.colors(
                        thumbColor = LimeAurora,
                        activeTrackColor = EmeraldGlow,
                        inactiveTrackColor = ForestPond
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text("B", color = LimeAurora, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SwipeView(
    imageA: Bitmap,
    imageB: Bitmap,
    swipeRatio: Float,
    zoomPercent: Float,
    panOffset: Offset,
    onZoomPanChanged: (Float, Offset) -> Unit,
    onSwipeChanged: (Float) -> Unit
) {
    var containerWidth by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(ForestDark.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                // Combined gesture mapping
                detectTransformGestures { _, pan, zoom, _ ->
                    val newZoom = (zoomPercent * zoom).coerceIn(100f, 1000f)
                    val newPan = panOffset + pan
                    onZoomPanChanged(newZoom, newPan)
                }
            }
            .layoutIdParent { width -> containerWidth = width },
        contentAlignment = Alignment.Center
    ) {
        // Base layer: Image A (Summer Forest)
        Image(
            bitmap = imageA.asImageBitmap(),
            contentDescription = "Image A Base Canvas",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoomPercent / 100f,
                    scaleY = zoomPercent / 100f,
                    translationX = panOffset.x,
                    translationY = panOffset.y
                )
        )

        // Overlay split: Image B clipped by SplitClipShape
        Image(
            bitmap = imageB.asImageBitmap(),
            contentDescription = "Image B Overlay Canvas",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoomPercent / 100f,
                    scaleY = zoomPercent / 100f,
                    translationX = panOffset.x,
                    translationY = panOffset.y
                )
                .clip(SplitClipShape(swipeRatio))
        )

        if (containerWidth > 0) {
            val density = LocalDensity.current
            val splitX = (containerWidth * swipeRatio)
            val splitXDp = with(density) { splitX.toDp() }

            // Unified draggable vertical frosted-glass splitter container (48dp width hit target)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = splitXDp - 24.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaRatio = dragAmount.x / containerWidth
                            val newRatio = (swipeRatio + deltaRatio).coerceIn(0f, 1f)
                            onSwipeChanged(newRatio)
                        }
                    }
            ) {
                // Split bar line inside (centered horizontally)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .align(Alignment.Center)
                        .background(LimeAurora.copy(alpha = 0.7f))
                )

                // Neon glass center drag handle matching 48dp criteria (centered)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                        .glass(cornerRadius = 24.dp, bgAlpha = 0.7f, borderAlpha = 0.9f)
                        .background(ForestVoid.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = "Slider Handler icon",
                        tint = LimeAurora,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(rotationZ = 90f)
                    )
                }
            }
        }
    }
}

/**
 * Simple modifier helper to extract layout parent width
 */
fun Modifier.layoutIdParent(onWidthFetched: (Float) -> Unit): Modifier = this.drawBehind {
    onWidthFetched(this.size.width)
}
