package gos.denver.facedetection.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.face.Face

class FaceGraphic(overlay: DetectorOverlayView, private val face: Face) : DetectorOverlayView.Graphic(overlay) {

    private val facePositionPaint: Paint = Paint()

    init {
        facePositionPaint.color = Color.WHITE
        facePositionPaint.style = Paint.Style.STROKE
        facePositionPaint.strokeWidth = BOX_STROKE_WIDTH
    }

    override fun draw(canvas: Canvas?) {
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
