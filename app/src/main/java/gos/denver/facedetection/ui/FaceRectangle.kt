package gos.denver.facedetection.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.face.Face

class FaceRectangle(
    private val face: Face, private val scaleFactor: Float,
    private val postScaleWidthOffset: Float, private val postScaleHeightOffset: Float,
    private val overlayWidth: Int,
    private val isImageFlipped: Boolean,
) {

    private val facePositionPaint: Paint = Paint()

    init {
        facePositionPaint.color = Color.WHITE
        facePositionPaint.style = Paint.Style.STROKE
        facePositionPaint.strokeWidth = BOX_STROKE_WIDTH
    }

    private fun scale(imagePixel: Float): Float {
        return imagePixel * scaleFactor
    }

    private fun translateX(x: Float): Float {
        return if (isImageFlipped) {
            overlayWidth - (scale(x) - postScaleWidthOffset)
        } else {
            scale(x) - postScaleWidthOffset
        }
    }

    private fun translateY(y: Float): Float {
        return scale(y) - postScaleHeightOffset
    }


    fun draw(canvas: Canvas?) {
        val x = translateX(face.boundingBox.centerX().toFloat())
        val y = translateY(face.boundingBox.centerY().toFloat())

        val left = x - scale(face.boundingBox.width() / 2.0f)
        val top = y - scale(face.boundingBox.height() / 2.0f)
        val right = x + scale(face.boundingBox.width() / 2.0f)
        val bottom = y + scale(face.boundingBox.height() / 2.0f)

        canvas?.drawRect(left, top, right, bottom, facePositionPaint)
    }

    private companion object {
        const val BOX_STROKE_WIDTH = 5.0f
    }
}
