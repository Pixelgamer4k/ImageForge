package com.imageforge.app.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.LinearGradient
import android.graphics.RadialGradient
import android.graphics.Shader

object SampleImages {

    /**
     * Generates a beautiful, highly detailed forest summer landscape Bitmap.
     */
    fun generateSummerForest(width: Int = 1200, height: Int = 800): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw sky gradient
        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height * 0.4f,
                0xFF1E293B.toInt(), 0xFF0D9488.toInt(), // Dark slate blue to Teal
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height * 0.4f, skyPaint)

        // Draw lake water gradient
        val waterPaint = Paint().apply {
            shader = LinearGradient(
                0f, height * 0.4f, 0f, height.toFloat(),
                0xFF0F172A.toInt(), 0xFF064E3B.toInt(), // Deep slate to dark forest pond
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, height * 0.4f, width.toFloat(), height.toFloat(), waterPaint)

        // Draw sun with radial glow
        val sunPaint = Paint().apply {
            isAntiAlias = true
            shader = RadialGradient(
                width * 0.75f, height * 0.2f, 250f,
                intArrayOf(0xFFFDE047.toInt(), 0x88FACC15.toInt(), 0x00F59E0B.toInt()),
                floatArrayOf(0f, 0.3f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width * 0.75f, height * 0.2f, 250f, sunPaint)

        // Draw sun rays
        val rayPaint = Paint().apply {
            isAntiAlias = true
            color = 0x1AFFF9C4.toInt()
            style = Paint.Style.FILL
        }
        for (i in 0..12) {
            val angle = i * Math.PI / 12
            val path = Path().apply {
                moveTo(width * 0.75f, height * 0.2f)
                val len = width.toFloat()
                lineTo((width * 0.75f + Math.cos(angle - 0.08) * len).toFloat(), (height * 0.2f + Math.sin(angle - 0.08) * len).toFloat())
                lineTo((width * 0.75f + Math.cos(angle + 0.08) * len).toFloat(), (height * 0.2f + Math.sin(angle + 0.08) * len).toFloat())
                close()
            }
            canvas.drawPath(path, rayPaint)
        }

        // Draw distant mountains
        val mountainPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF042F1A.toInt() // Very dark moss
        }
        val mountainPath = Path().apply {
            moveTo(0f, height * 0.45f)
            lineTo(width * 0.2f, height * 0.32f)
            lineTo(width * 0.45f, height * 0.4f)
            lineTo(width * 0.7f, height * 0.25f)
            lineTo(width * 0.9f, height * 0.38f)
            lineTo(width.toFloat(), height * 0.42f)
            lineTo(width.toFloat(), height.toFloat())
            lineTo(0f, height.toFloat())
            close()
        }
        canvas.drawPath(mountainPath, mountainPaint)

        // Draw pine forest rows (Layer 1 - Dark Teal-Green)
        val pinePaint1 = Paint().apply {
            isAntiAlias = true
            color = 0xFF065F46.toInt()
        }
        drawPineTrees(canvas, pinePaint1, width, height, treeSpacing = 60, startY = height * 0.48f, scale = 1.0f)

        // Draw pine forest rows (Layer 2 - Vibrant Emerald)
        val pinePaint2 = Paint().apply {
            isAntiAlias = true
            color = 0xFF10B981.toInt()
        }
        drawPineTrees(canvas, pinePaint2, width, height, treeSpacing = 90, startY = height * 0.58f, scale = 1.4f)

        // Draw water ripples
        val ripplePaint = Paint().apply {
            isAntiAlias = true
            color = 0x3334D399.toInt() // Soft translucent emerald
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(width * 0.1f, height * 0.6f, width * 0.3f, height * 0.6f, ripplePaint)
        canvas.drawLine(width * 0.5f, height * 0.72f, width * 0.85f, height * 0.72f, ripplePaint)
        canvas.drawLine(width * 0.2f, height * 0.85f, width * 0.45f, height * 0.85f, ripplePaint)

        return bitmap
    }

    /**
     * Generates a matching autumn misty landscape for rigorous side-by-side/split comparison.
     * Keeps spatial structures exactly matched, but mutates colors to represent a chilling mist.
     */
    fun generateAutumnMistForest(width: Int = 1200, height: Int = 800): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw misty sky gradient (grey-blue cold sky)
        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height * 0.4f,
                0xFF475569.toInt(), 0xFF64748B.toInt(), // Cool grey slates
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height * 0.4f, skyPaint)

        // Draw misty water (low contrast cool reflection)
        val waterPaint = Paint().apply {
            shader = LinearGradient(
                0f, height * 0.4f, 0f, height.toFloat(),
                0xFF334155.toInt(), 0xFF1E293B.toInt(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, height * 0.4f, width.toFloat(), height.toFloat(), waterPaint)

        // Draw weak diffused sun
        val sunPaint = Paint().apply {
            isAntiAlias = true
            shader = RadialGradient(
                width * 0.75f, height * 0.2f, 280f,
                intArrayOf(0xBBFDE047.toInt(), 0x44CBD5E1.toInt(), 0x00E2E8F0.toInt()),
                floatArrayOf(0f, 0.4f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width * 0.75f, height * 0.2f, 280f, sunPaint)

        // Draw distant mountains (Faded cool grey-blue)
        val mountainPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF1E293B.toInt()
        }
        val mountainPath = Path().apply {
            moveTo(0f, height * 0.45f)
            lineTo(width * 0.2f, height * 0.32f)
            lineTo(width * 0.45f, height * 0.4f)
            lineTo(width * 0.7f, height * 0.25f)
            lineTo(width * 0.9f, height * 0.38f)
            lineTo(width.toFloat(), height * 0.42f)
            lineTo(width.toFloat(), height.toFloat())
            lineTo(0f, height.toFloat())
            close()
        }
        canvas.drawPath(mountainPath, mountainPaint)

        // Draw pine forest rows (Layer 1 - Golden Orange/Ochre)
        val pinePaint1 = Paint().apply {
            isAntiAlias = true
            color = 0xFF9A3412.toInt() // Dark terracotta orange
        }
        drawPineTrees(canvas, pinePaint1, width, height, treeSpacing = 60, startY = height * 0.48f, scale = 1.0f)

        // Draw pine forest rows (Layer 2 - Vibrant Amber/Gold)
        val pinePaint2 = Paint().apply {
            isAntiAlias = true
            color = 0xFFD97706.toInt() // Warm amber
        }
        drawPineTrees(canvas, pinePaint2, width, height, treeSpacing = 90, startY = height * 0.58f, scale = 1.4f)

        // Overlay a heavy mist layer (Horizontal linear gradient sweep)
        val mistPaint = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                0f, height * 0.3f, 0f, height.toFloat(),
                0x00CBD5E1.toInt(), 0xAAB7C3D0.toInt(), // Soft fog gradient
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, height * 0.3f, width.toFloat(), height.toFloat(), mistPaint)

        // Draw water ripples (frosty reflections)
        val ripplePaint = Paint().apply {
            isAntiAlias = true
            color = 0x44CBD5E1.toInt()
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(width * 0.1f, height * 0.6f, width * 0.3f, height * 0.6f, ripplePaint)
        canvas.drawLine(width * 0.5f, height * 0.72f, width * 0.85f, height * 0.72f, ripplePaint)
        canvas.drawLine(width * 0.2f, height * 0.85f, width * 0.45f, height * 0.85f, ripplePaint)

        return bitmap
    }

    private fun drawPineTrees(
        canvas: Canvas,
        paint: Paint,
        width: Int,
        height: Int,
        treeSpacing: Int,
        startY: Float,
        scale: Float
    ) {
        var x = 20f
        while (x < width) {
            // Draw a single multi-layered pine triangle
            val treeW = 35f * scale
            val treeH = 75f * scale
            val treeY = startY + (Math.sin(x.toDouble()) * 12f).toFloat() // Subtle natural offsets

            val path = Path().apply {
                // Top layer
                moveTo(x, treeY - treeH)
                lineTo(x - treeW * 0.5f, treeY - treeH * 0.5f)
                lineTo(x + treeW * 0.5f, treeY - treeH * 0.5f)
                close()

                // Middle layer
                moveTo(x, treeY - treeH * 0.6f)
                lineTo(x - treeW * 0.8f, treeY - treeH * 0.1f)
                lineTo(x + treeW * 0.8f, treeY - treeH * 0.1f)
                close()

                // Bottom layer
                moveTo(x, treeY - treeH * 0.25f)
                lineTo(x - treeW, treeY + treeH * 0.2f)
                lineTo(x + treeW, treeY + treeH * 0.2f)
                close()
            }
            canvas.drawPath(path, paint)

            x += treeSpacing + (Math.cos(x.toDouble()) * 15f).toFloat() // Organic variation
        }
    }
}
