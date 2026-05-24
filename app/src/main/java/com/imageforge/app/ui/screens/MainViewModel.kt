package com.imageforge.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imageforge.app.data.SampleImages
import com.imageforge.app.ui.theme.EmeraldGlow
import com.imageforge.app.ui.theme.LimeAurora
import com.imageforge.app.ui.theme.TealMist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main Comparison Layout Modes
 */
enum class ComparisonMode {
    SIDE_BY_SIDE, OVERLAY, SWIPE
}

/**
 * Simulated LUT / Color Shaders
 */
enum class FilterShader {
    NONE, FOREST_ENHANCE, FALSE_COLOR, HIGH_CONTRAST, MONOCHROME
}

/**
 * sealed interface representing our main UI States
 */
sealed interface MainUiState {
    object Loading : MainUiState
    data class Ready(
        val imageA: Bitmap,
        val imageB: Bitmap,
        val processedA: Bitmap,
        val processedB: Bitmap,
        val activeMode: ComparisonMode = ComparisonMode.SWIPE,
        val overlayRatio: Float = 0.5f,
        val swipeRatio: Float = 0.5f,
        val zoomPercent: Float = 100f,
        val panOffset: Offset = Offset.Zero,
        val isSynced: Boolean = true,
        
        // Active Filters
        val peakingEnabled: Boolean = false,
        val edgesEnabled: Boolean = false,
        val filterShader: FilterShader = FilterShader.NONE,
        val flipHorizontal: Boolean = false,
        val flipVertical: Boolean = false,
        val rotateDegrees: Float = 0f,
        val colorsPalette: List<Color> = listOf(EmeraldGlow, LimeAurora, TealMist),

        // Tabs
        val activeAnalysisTab: Int = 0, // 0 = Hist, 1 = Wave, 2 = Vector
        val isProcessing: Boolean = false,

        // Analysis Datasets
        val histogramData: HistogramData = HistogramData(),
        val waveformData: WaveformData = WaveformData(),
        val vectorscopeData: VectorscopeData = VectorscopeData()
    ) : MainUiState
}

/**
 * Formatted analytical structures for canvas drawing
 */
data class HistogramData(
    val redChannel: IntArray = IntArray(256),
    val greenChannel: IntArray = IntArray(256),
    val blueChannel: IntArray = IntArray(256),
    val maxValue: Int = 1
)

data class WaveformData(
    val luminanceBins: Array<IntArray> = Array(128) { IntArray(100) },
    val maxValue: Int = 1
)

data class VectorscopeData(
    val points: List<Offset> = emptyList() // List of saturated chrominance coordinate offsets
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Cache base bitmaps
    private lateinit var baseImageA: Bitmap
    private lateinit var baseImageB: Bitmap

    init {
        loadImages()
    }

    private fun loadImages() {
        viewModelScope.launch(Dispatchers.Default) {
            baseImageA = SampleImages.generateSummerForest(800, 500)
            baseImageB = SampleImages.generateAutumnMistForest(800, 500)
            
            // Extract colors for the palette
            val palette = extractColors(baseImageA)

            val readyState = MainUiState.Ready(
                imageA = baseImageA,
                imageB = baseImageB,
                processedA = baseImageA,
                processedB = baseImageB,
                colorsPalette = palette
            )
            _uiState.value = readyState
            
            // Trigger processing for analysis and filters
            processImagesPipeline()
        }
    }

    /**
     * Set active comparison mode
     */
    fun setComparisonMode(mode: ComparisonMode) {
        _uiState.update { state ->
            if (state is MainUiState.Ready) state.copy(activeMode = mode) else state
        }
    }

    /**
     * Update ratios (Slider triggers)
     */
    fun setOverlayRatio(ratio: Float) {
        _uiState.update { state ->
            if (state is MainUiState.Ready) state.copy(overlayRatio = ratio) else state
        }
    }

    fun setSwipeRatio(ratio: Float) {
        _uiState.update { state ->
            if (state is MainUiState.Ready) state.copy(swipeRatio = ratio) else state
        }
    }

    /**
     * Zoom & Pan manipulations
     */
    fun updateZoomAndPan(zoom: Float, pan: Offset) {
        _uiState.update { state ->
            if (state is MainUiState.Ready) {
                state.copy(
                    zoomPercent = zoom.coerceIn(100f, 1000f),
                    panOffset = pan
                )
            } else state
        }
    }

    fun toggleSync() {
        _uiState.update { state ->
            if (state is MainUiState.Ready) state.copy(isSynced = !state.isSynced) else state
        }
    }

    fun resetZoom() {
        _uiState.update { state ->
            if (state is MainUiState.Ready) state.copy(zoomPercent = 100f, panOffset = Offset.Zero) else state
        }
    }

    /**
     * Right Toolbar Filters Toggles
     */
    fun togglePeaking() {
        _uiState.update { state ->
            if (state is MainUiState.Ready) state.copy(peakingEnabled = !state.peakingEnabled) else state
        }
        processImagesPipeline()
    }

    fun toggleEdges() {
        _uiState.update { state ->
            if (state is MainUiState.Ready) state.copy(edgesEnabled = !state.edgesEnabled) else state
        }
        processImagesPipeline()
    }

    fun cycleFilterShader() {
        _uiState.update { state ->
            if (state is MainUiState.Ready) {
                val nextShader = when (state.filterShader) {
                    FilterShader.NONE -> FilterShader.FOREST_ENHANCE
                    FilterShader.FOREST_ENHANCE -> FilterShader.FALSE_COLOR
                    FilterShader.FALSE_COLOR -> FilterShader.HIGH_CONTRAST
                    FilterShader.HIGH_CONTRAST -> FilterShader.MONOCHROME
                    FilterShader.MONOCHROME -> FilterShader.NONE
                }
                state.copy(filterShader = nextShader)
            } else state
        }
        processImagesPipeline()
    }

    fun toggleFlipHorizontal() {
        _uiState.update { state ->
            if (state is MainUiState.Ready) state.copy(flipHorizontal = !state.flipHorizontal) else state
        }
        processImagesPipeline()
    }

    fun toggleFlipVertical() {
        _uiState.update { state ->
            if (state is MainUiState.Ready) state.copy(flipVertical = !state.flipVertical) else state
        }
        processImagesPipeline()
    }

    fun rotate90() {
        _uiState.update { state ->
            if (state is MainUiState.Ready) {
                val nextRotation = (state.rotateDegrees + 90f) % 360f
                state.copy(rotateDegrees = nextRotation)
            } else state
        }
        processImagesPipeline()
    }

    fun swapImages() {
        viewModelScope.launch(Dispatchers.Default) {
            val temp = baseImageA
            baseImageA = baseImageB
            baseImageB = temp
            
            val palette = extractColors(baseImageA)
            _uiState.update { state ->
                if (state is MainUiState.Ready) {
                    state.copy(
                        imageA = baseImageA,
                        imageB = baseImageB,
                        colorsPalette = palette
                    )
                } else state
            }
            processImagesPipeline()
        }
    }

    fun setAnalysisTab(index: Int) {
        _uiState.update { state ->
            if (state is MainUiState.Ready) state.copy(activeAnalysisTab = index) else state
        }
    }

    /**
     * Asynchronous Image Filter Processing Pipeline + Statistics calculations
     */
    private fun processImagesPipeline() {
        val currentState = _uiState.value as? MainUiState.Ready ?: return
        
        _uiState.update { if (it is MainUiState.Ready) it.copy(isProcessing = true) else it }

        viewModelScope.launch(Dispatchers.Default) {
            // Apply scale / rot / flip transforms first
            val transA = applyTransformations(baseImageA, currentState)
            val transB = applyTransformations(baseImageB, currentState)

            // Apply shaders (Peaking, Edges, LUTs)
            val finalA = applyShaders(transA, currentState)
            val finalB = applyShaders(transB, currentState)

            // Calculate graphics analysis data
            val histogram = calculateHistogram(finalA, finalB)
            val waveform = calculateWaveform(finalA)
            val vectorscope = calculateVectorscope(finalA)

            _uiState.update { state ->
                if (state is MainUiState.Ready) {
                    state.copy(
                        processedA = finalA,
                        processedB = finalB,
                        histogramData = histogram,
                        waveformData = waveform,
                        vectorscopeData = vectorscope,
                        isProcessing = false
                    )
                } else state
            }
        }
    }

    private suspend fun applyTransformations(src: Bitmap, state: MainUiState.Ready): Bitmap = withContext(Dispatchers.Default) {
        if (!state.flipHorizontal && !state.flipVertical && state.rotateDegrees == 0f) return@withContext src

        val matrix = android.graphics.Matrix()
        if (state.flipHorizontal) matrix.postScale(-1f, 1f, src.width / 2f, src.height / 2f)
        if (state.flipVertical) matrix.postScale(1f, -1f, src.width / 2f, src.height / 2f)
        if (state.rotateDegrees != 0f) matrix.postRotate(state.rotateDegrees)

        Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    private suspend fun applyShaders(src: Bitmap, state: MainUiState.Ready): Bitmap = withContext(Dispatchers.Default) {
        if (!state.peakingEnabled && !state.edgesEnabled && state.filterShader == FilterShader.NONE) return@withContext src

        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        val outPixels = IntArray(width * height)

        // 1. Process color filters and edge shaders
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                val c = pixels[idx]
                val r = AndroidColor.red(c)
                val g = AndroidColor.green(c)
                val b = AndroidColor.blue(c)

                var finalR = r
                var finalG = g
                var finalB = b

                // Apply LUT filter mode
                when (state.filterShader) {
                    FilterShader.FOREST_ENHANCE -> {
                        // Boost green, lower red and blue
                        finalG = (g * 1.35f).toInt().coerceIn(0, 255)
                        finalR = (r * 0.85f).toInt().coerceIn(0, 255)
                        finalB = (b * 0.90f).toInt().coerceIn(0, 255)
                    }
                    FilterShader.FALSE_COLOR -> {
                        // Thermal luminance map
                        val lum = (0.299f * r + 0.587f * g + 0.114f * b)
                        if (lum < 50) {
                            finalR = 0
                            finalG = 0
                            finalB = (lum * 5.1f).toInt().coerceIn(0, 255)
                        } else if (lum < 130) {
                            finalR = 0
                            finalG = ((lum - 50) * 3.1f).toInt().coerceIn(0, 255)
                            finalB = (255 - (lum - 50) * 3).toInt().coerceIn(0, 255)
                        } else if (lum < 200) {
                            finalR = ((lum - 130) * 3.6f).toInt().coerceIn(0, 255)
                            finalG = 255
                            finalB = 0
                        } else {
                            finalR = 255
                            finalG = (255 - (lum - 200) * 4.6f).toInt().coerceIn(0, 255)
                            finalB = 0
                        }
                    }
                    FilterShader.HIGH_CONTRAST -> {
                        // Sigmoid contrast stretch
                        val factor = 1.8f
                        finalR = (((r - 128) * factor) + 128).toInt().coerceIn(0, 255)
                        finalG = (((g - 128) * factor) + 128).toInt().coerceIn(0, 255)
                        finalB = (((b - 128) * factor) + 128).toInt().coerceIn(0, 255)
                    }
                    FilterShader.MONOCHROME -> {
                        val gray = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
                        finalR = gray
                        finalG = gray
                        finalB = gray
                    }
                    FilterShader.NONE -> {}
                }

                outPixels[idx] = AndroidColor.rgb(finalR, finalG, finalB)
            }
        }

        // 2. Apply high gradient highlighting (Peaking / Edges)
        if (state.peakingEnabled || state.edgesEnabled) {
            val resultPixels = IntArray(width * height)
            val peakingThreshold = 25
            
            for (y in 1 until height - 1) {
                for (x in 1 until width - 1) {
                    val idx = y * width + x

                    // Sobel / Gradient approximation
                    val centerLum = getLuminance(outPixels[idx])
                    val rightLum = getLuminance(outPixels[idx + 1])
                    val bottomLum = getLuminance(outPixels[idx + width])
                    
                    val grad = Math.abs(centerLum - rightLum) + Math.abs(centerLum - bottomLum)

                    if (state.edgesEnabled) {
                        // High-contrast outline rendering
                        val edgeColor = if (grad > peakingThreshold) 255 else 0
                        resultPixels[idx] = AndroidColor.rgb(edgeColor, edgeColor, edgeColor)
                    } else if (state.peakingEnabled && grad > peakingThreshold * 1.5) {
                        // Draw vibrant Neon Green peaking outline
                        resultPixels[idx] = AndroidColor.rgb(16, 235, 129)
                    } else {
                        resultPixels[idx] = outPixels[idx]
                    }
                }
            }
            return@withContext Bitmap.createBitmap(resultPixels, width, height, Bitmap.Config.ARGB_8888)
        }

        Bitmap.createBitmap(outPixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun getLuminance(color: Int): Int {
        val r = AndroidColor.red(color)
        val g = AndroidColor.green(color)
        val b = AndroidColor.blue(color)
        return (0.299f * r + 0.587f * g + 0.114f * b).toInt()
    }

    /**
     * Compute Histogram frequencies for Red, Green, and Blue
     */
    private suspend fun calculateHistogram(imgA: Bitmap, imgB: Bitmap): HistogramData = withContext(Dispatchers.Default) {
        val rChan = IntArray(256)
        val gChan = IntArray(256)
        val bChan = IntArray(256)

        // Downsample for speedy computation (take every 5th pixel)
        val width = imgA.width
        val height = imgA.height
        
        for (y in 0 until height step 4) {
            for (x in 0 until width step 4) {
                val pixel = imgA.getPixel(x, y)
                val r = AndroidColor.red(pixel)
                val g = AndroidColor.green(pixel)
                val b = AndroidColor.blue(pixel)

                rChan[r]++
                gChan[g]++
                bChan[b]++
            }
        }

        var maxVal = 1
        for (i in 0..255) {
            maxVal = maxOf(maxVal, rChan[i], gChan[i], bChan[i])
        }

        HistogramData(rChan, gChan, bChan, maxVal)
    }

    /**
     * Compute Waveform luminance vectors spanning horizontal grid
     */
    private suspend fun calculateWaveform(img: Bitmap): WaveformData = withContext(Dispatchers.Default) {
        val columns = 128
        val rows = 100
        val bins = Array(columns) { IntArray(rows) }

        val width = img.width
        val height = img.height

        for (c in 0 until columns) {
            val srcX = (c * (width - 1)) / (columns - 1)
            for (y in 0 until height step 4) {
                val pixel = img.getPixel(srcX, y)
                val lum = getLuminance(pixel)
                val rIdx = (lum * (rows - 1)) / 255
                bins[c][rIdx]++
            }
        }

        var maxVal = 1
        for (c in 0 until columns) {
            for (r in 0 until rows) {
                maxVal = maxOf(maxVal, bins[c][r])
            }
        }

        WaveformData(bins, maxVal)
    }

    /**
     * Compute Vectorscope chromatic angle offsets
     */
    private suspend fun calculateVectorscope(img: Bitmap): VectorscopeData = withContext(Dispatchers.Default) {
        val width = img.width
        val height = img.height
        val points = mutableListOf<Offset>()

        // Draw 300 downsampled chromatic points based on Cb/Cr formulas
        val stepX = width / 18
        val stepY = height / 18

        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                val pixel = img.getPixel(x, y)
                val r = AndroidColor.red(pixel)
                val g = AndroidColor.green(pixel)
                val b = AndroidColor.blue(pixel)

                // Cb / Cr coordinates (offset from center)
                val cr = (0.5f * r - 0.4187f * g - 0.0813f * b) / 128f
                val cb = (-0.1687f * r - 0.3313f * g + 0.5f * b) / 128f

                // Map -1..1 range to coordinates in vectorscope box (-50f..50f relative to center)
                points.add(Offset(cb * 80f, -cr * 80f))
            }
        }

        VectorscopeData(points)
    }

    /**
     * Extracts a dominant color palette from the image
     */
    private fun extractColors(bitmap: Bitmap): List<Color> {
        val width = bitmap.width
        val height = bitmap.height
        val colors = mutableMapOf<Int, Int>()

        // Check center region of bitmap
        val stepX = width / 12
        val stepY = height / 12
        for (y in (height / 4)..(3 * height / 4) step stepY) {
            for (x in (width / 4)..(3 * width / 4) step stepX) {
                val pixel = bitmap.getPixel(x, y)
                colors[pixel] = (colors[pixel] ?: 0) + 1
            }
        }

        val sorted = colors.entries.sortedByDescending { it.value }
        val output = mutableListOf<Color>()

        // Default fallbacks in case of low variance
        val defaultColors = listOf(EmeraldGlow, LimeAurora, TealMist)
        
        var addedCount = 0
        for (entry in sorted) {
            val keyColor = Color(entry.key)
            // Filter extreme black/white
            val androidColor = entry.key
            val r = AndroidColor.red(androidColor)
            val g = AndroidColor.green(androidColor)
            val b = AndroidColor.blue(androidColor)
            val isExtreme = (r < 25 && g < 25 && b < 25) || (r > 230 && g > 230 && b > 230)
            
            if (!isExtreme) {
                output.add(keyColor)
                addedCount++
                if (addedCount >= 3) break
            }
        }

        while (output.size < 3) {
            output.add(defaultColors[output.size])
        }

        return output
    }
}
