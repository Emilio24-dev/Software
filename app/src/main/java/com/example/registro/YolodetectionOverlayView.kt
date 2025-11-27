package com.example.registro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

data class YoloBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val label: String,
    val confidence: Float,
    // Coordenadas normalizadas 0..1
    val imageWidth: Int,
    val imageHeight: Int
)

class YoloDetectionsOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val boxes = mutableListOf<YoloBox>()

    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        textSize = 32f
        isAntiAlias = true
    }

    fun setDetections(newBoxes: List<YoloBox>) {
        boxes.clear()
        boxes.addAll(newBoxes)
        invalidate()
    }

    fun clearDetections() {
        boxes.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (boxes.isEmpty()) return

        val viewW = width.toFloat()
        val viewH = height.toFloat()

        for (box in boxes) {
            // Convertir coords originales (en pixeles de la imagen) a coords del View
            val scaleX = viewW / box.imageWidth
            val scaleY = viewH / box.imageHeight

            val left = box.x1 * scaleX
            val top = box.y1 * scaleY
            val right = box.x2 * scaleX
            val bottom = box.y2 * scaleY

            val rect = RectF(left, top, right, bottom)

            // Color por defecto (blanco)
            boxPaint.color = 0xFFFFFFFF.toInt()
            textPaint.color = 0xFFFFFFFF.toInt()

            canvas.drawRect(rect, boxPaint)

            val text = "${box.label} ${(box.confidence * 100).toInt()}%"
            canvas.drawText(text, left, top - 8f, textPaint)
        }
    }
}
