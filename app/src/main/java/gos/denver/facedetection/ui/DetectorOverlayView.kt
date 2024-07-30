package gos.denver.facedetection.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View

class DetectorOverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private val lock = Any()
    private val faceRectangles: MutableList<FaceRectangle> = mutableListOf()

    private val transformationMatrix = Matrix()

    private var imageWidth: Int = 0
    private var imageHeight: Int = 0

    var scaleFactor = 1.0f
        private set

    var postScaleWidthOffset = 0f
        private set

    var postScaleHeightOffset = 0f
        private set

    var isImageFlipped = false
        private set

    private var needUpdateTransformation = true


    init {
        addOnLayoutChangeListener { _: View?, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int ->
            needUpdateTransformation = true
        }
    }

    fun clear() {
        synchronized(lock) {
            faceRectangles.clear()
        }
        postInvalidate()
    }

    fun add(graphic: FaceRectangle) {
        synchronized(lock) {
            faceRectangles.add(graphic)
        }
    }

    fun setImageSourceInfo(imageWidth: Int, imageHeight: Int, isFlipped: Boolean) {
        synchronized(lock) {
            this.imageWidth = imageWidth
            this.imageHeight = imageHeight
            this.isImageFlipped = isFlipped
            needUpdateTransformation = true
        }
        postInvalidate()
    }

    private fun updateTransformationIfNeeded() {
        if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0) {
            return
        }
        val viewAspectRatio = width.toFloat() / height
        val imageAspectRatio = imageWidth.toFloat() / imageHeight
        postScaleWidthOffset = 0f
        postScaleHeightOffset = 0f

        if (viewAspectRatio > imageAspectRatio) {
            scaleFactor = width.toFloat() / imageWidth
            postScaleHeightOffset = (width.toFloat() / imageAspectRatio - height) / 2
        } else {
            scaleFactor = height.toFloat() / imageHeight
            postScaleWidthOffset = (height.toFloat() * imageAspectRatio - width) / 2
        }

        transformationMatrix.reset()
        transformationMatrix.setScale(scaleFactor, scaleFactor)
        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset)

        if (isImageFlipped) {
            transformationMatrix.postScale(-1f, 1f, width / 2f, height / 2f)
        }

        needUpdateTransformation = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        synchronized(lock) {
            updateTransformationIfNeeded()
            for (graphic in faceRectangles) {
                graphic.draw(canvas)
            }
        }
    }
}

