package com.imageforge.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imageforge.app.ui.components.*
import com.imageforge.app.ui.theme.*
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pickerLauncherA = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                val bitmap = uriToBitmap(context, it)
                if (bitmap != null) {
                    viewModel.setImageA(bitmap)
                }
            }
        }
    }

    val pickerLauncherB = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                val bitmap = uriToBitmap(context, it)
                if (bitmap != null) {
                    viewModel.setImageB(bitmap)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestVoid)
    ) {
        // 1. Forest Organic Shifting Liquid Backdrop
        OrganicLiquidBackground()

        when (val state = uiState) {
            is MainUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmeraldGlow)
                }
            }
            is MainUiState.Ready -> {
                // Main layout columns
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // TOP BAR
                    TopAppBarSection(
                        isSynced = state.isSynced,
                        zoomPercent = state.zoomPercent,
                        onSyncToggled = { viewModel.toggleSync() },
                        onResetZoom = { viewModel.resetZoom() }
                    )

                    // CENTER COMPARISON DOCK
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Viewport Box
                        ComparisonArea(
                            mode = state.activeMode,
                            imageA = state.processedA,
                            imageB = state.processedB,
                            overlayRatio = state.overlayRatio,
                            swipeRatio = state.swipeRatio,
                            zoomPercent = state.zoomPercent,
                            panOffset = state.panOffset,
                            isSynced = state.isSynced,
                            onRatioChanged = { viewModel.setOverlayRatio(it) },
                            onSwipeChanged = { viewModel.setSwipeRatio(it) },
                            onZoomPanChanged = { zoom, pan -> viewModel.updateZoomAndPan(zoom, pan) },
                            onLoadImageA = { pickerLauncherA.launch("image/*") },
                            onLoadImageB = { pickerLauncherB.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        )

                        // Floating Right vertical glass toolbar
                        RightToolbarSection(
                            peakingEnabled = state.peakingEnabled,
                            edgesEnabled = state.edgesEnabled,
                            shaderMode = state.filterShader,
                            flipH = state.flipHorizontal,
                            flipV = state.flipVertical,
                            rotateDeg = state.rotateDegrees,
                            palette = state.colorsPalette,
                            onPeakingClick = { viewModel.togglePeaking() },
                            onEdgesClick = { viewModel.toggleEdges() },
                            onFilterClick = { viewModel.cycleFilterShader() },
                            onFlipHClick = { viewModel.toggleFlipHorizontal() },
                            onFlipVClick = { viewModel.toggleFlipVertical() },
                            onRotateClick = { viewModel.rotate90() },
                            onSwapClick = { viewModel.swapImages() },
                            onSyncClick = { viewModel.toggleSync() }
                        )
                    }

                    // BOTTOM SEGMENTED CONTROL DOCK + COLLAPSIBLE ANALYSIS
                    BottomDockSection(
                        activeTab = state.activeAnalysisTab,
                        onTabSelected = { viewModel.setAnalysisTab(it) },
                        onModeSelected = { viewModel.setComparisonMode(it) },
                        state = state
                    )
                }

                // Global Processing Loader indicator
                if (state.isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BlackTranslucent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .glass(cornerRadius = 16.dp, bgAlpha = 0.6f, borderAlpha = 0.6f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = LimeAurora, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                                Text("Analyzing Pixels...", color = WhiteTranslucent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top Status Bar layout
 */
@Composable
fun TopAppBarSection(
    isSynced: Boolean,
    zoomPercent: Float,
    onSyncToggled: () -> Unit,
    onResetZoom: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left back button round glass target
        Box(
            modifier = Modifier
                .size(44.dp)
                .glass(cornerRadius = 22.dp, bgAlpha = 0.3f, borderAlpha = 0.5f)
                .clickable { /* Handle back stack exit */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Navigate back icon",
                tint = WhiteTranslucent,
                modifier = Modifier.size(20.dp)
            )
        }

        // Center app Title
        Text(
            text = "IMAGEFORGE",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        // SYNCED capsulated pill with animated pulse
        Box(
            modifier = Modifier
                .glass(cornerRadius = 16.dp, bgAlpha = 0.35f, borderAlpha = 0.6f)
                .clickable { onSyncToggled() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Pulsating green indicator dot
                val transition = rememberInfiniteTransition(label = "SyncPulse")
                val pulseAlpha by transition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "PulseAlpha"
                )

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = if (isSynced) EmeraldGlow.copy(alpha = pulseAlpha) else ErrorGlow.copy(alpha = pulseAlpha),
                            shape = CircleShape
                        )
                )

                Text(
                    text = if (isSynced) "${zoomPercent.toInt()}% SYNCED" else "ASYNC VIEWPORT",
                    color = if (isSynced) LimeAurora else ErrorGlow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Floating vertical GlassToolbar section
 */
@Composable
fun RightToolbarSection(
    peakingEnabled: Boolean,
    edgesEnabled: Boolean,
    shaderMode: FilterShader,
    flipH: Boolean,
    flipV: Boolean,
    rotateDeg: Float,
    palette: List<Color>,
    onPeakingClick: () -> Unit,
    onEdgesClick: () -> Unit,
    onFilterClick: () -> Unit,
    onFlipHClick: () -> Unit,
    onFlipVClick: () -> Unit,
    onRotateClick: () -> Unit,
    onSwapClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    GlassToolbar {
        GlassToolbarButton(
            icon = Icons.Default.Adjust,
            contentDescription = "Peaking overlay highlight",
            isActive = peakingEnabled,
            onClick = onPeakingClick
        )

        GlassToolbarButton(
            icon = Icons.Default.GridOn,
            contentDescription = "Infinite Zoom bounder",
            isActive = false,
            onClick = onSyncClick
        )

        GlassToolbarButton(
            icon = Icons.Default.BlurOn,
            contentDescription = "Sobel Edge detector shader",
            isActive = edgesEnabled,
            onClick = onEdgesClick
        )

        GlassToolbarButton(
            icon = Icons.Default.ColorLens,
            contentDescription = "LUT Filters cyclic shifter",
            isActive = shaderMode != FilterShader.NONE,
            onClick = onFilterClick,
            badgeText = when(shaderMode) {
                FilterShader.FOREST_ENHANCE -> "FST"
                FilterShader.FALSE_COLOR -> "FLS"
                FilterShader.HIGH_CONTRAST -> "CTR"
                FilterShader.MONOCHROME -> "MON"
                FilterShader.NONE -> null
            }
        )

        Divider(color = WhiteGlassBorder.copy(alpha = 0.05f), modifier = Modifier.width(30.dp))

        GlassToolbarButton(
            icon = Icons.Default.SwapHoriz,
            contentDescription = "Flip Horizontally toggle",
            isActive = flipH,
            onClick = onFlipHClick
        )

        GlassToolbarButton(
            icon = Icons.Default.SwapVert,
            contentDescription = "Flip Vertically toggle",
            isActive = flipV,
            onClick = onFlipVClick
        )

        GlassToolbarButton(
            icon = Icons.Default.RotateRight,
            contentDescription = "Rotate 90 degrees",
            isActive = rotateDeg != 0f,
            onClick = onRotateClick,
            badgeText = if (rotateDeg > 0f) "${rotateDeg.toInt()}°" else null
        )

        Divider(color = WhiteGlassBorder.copy(alpha = 0.05f), modifier = Modifier.width(30.dp))

        GlassToolbarButton(
            icon = Icons.Default.CompareArrows,
            contentDescription = "Swap base images",
            isActive = false,
            onClick = onSwapClick
        )

        // Dominant Color Palette Indicator lights at the bottom of the toolbar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            palette.forEach { col ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(col, CircleShape)
                        .border(1.dp, WhiteGlassBorder.copy(alpha = 0.3f), CircleShape)
                )
            }
        }
    }
}

/**
 * Bottom Dock Section containing tabs and expandable Canvas statistics dashboard
 */
@Composable
fun BottomDockSection(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    onModeSelected: (ComparisonMode) -> Unit,
    state: MainUiState.Ready
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Floating layout selector segments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Select buttons
            Row(
                modifier = Modifier
                    .weight(1f)
                    .glass(cornerRadius = 24.dp, bgAlpha = 0.25f, borderAlpha = 0.35f)
                    .height(52.dp)
                    .padding(4.dp)
            ) {
                val modes = listOf(
                    Triple(ComparisonMode.SIDE_BY_SIDE, "SIDE", Icons.Default.ViewWeek),
                    Triple(ComparisonMode.OVERLAY, "OVER", Icons.Default.Layers),
                    Triple(ComparisonMode.SWIPE, "SPLIT", Icons.Default.Compare)
                )

                modes.forEach { (mode, label, icon) ->
                    val isSel = state.activeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) EmeraldGlow.copy(alpha = 0.25f) else Color.Transparent)
                            .border(1.dp, if (isSel) LimeAurora.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable { onModeSelected(mode) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, null, tint = if (isSel) LimeAurora else WhiteTranslucent, modifier = Modifier.size(14.dp))
                            Text(label, color = if (isSel) LimeAurora else WhiteTranslucent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tab switch control (Hist • Wave • Vector)
            GlassSegmentedControl(
                items = listOf("Hist", "Wave", "Vector"),
                selectedIndex = activeTab,
                onItemSelected = onTabSelected,
                modifier = Modifier.width(220.dp)
            )
        }

        // Analysis panel card matching sliding animation
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AnalysisPanel(
                activeTab = activeTab,
                histogramData = state.histogramData,
                waveformData = state.waveformData,
                vectorscopeData = state.vectorscopeData
            )
        }
    }
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)?.copy(Bitmap.Config.ARGB_8888, true)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
