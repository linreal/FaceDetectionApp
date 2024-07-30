package gos.denver.facedetection.ui

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class CameraManager {
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    private var lifecycleOwner: LifecycleOwner? = null
    private var surfaceProvider: Preview.SurfaceProvider? = null
    private var onNextFrameAvailable: ((ImageProxy) -> Unit)? = null
    private var updateGraphicOverlayImageSourceInfo: ((Int, Int, Boolean) -> Unit)? = null

    fun init(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        onNextFrameAvailable: (ImageProxy) -> Unit,
        updateGraphicOverlayImageSourceInfo: (Int, Int, Boolean) -> Unit,
        lensFacing: Int
    ) {
        if (this.cameraProvider != null) {
            return
        }
        this.lifecycleOwner = lifecycleOwner
        this.surfaceProvider = surfaceProvider
        this.onNextFrameAvailable = onNextFrameAvailable
        this.updateGraphicOverlayImageSourceInfo = updateGraphicOverlayImageSourceInfo
        this.cameraProvider = cameraProvider
        this.lensFacing = lensFacing
        bindUseCases()
    }

    fun switchCamera(lenFacing: Int) {
        if (lenFacing == lensFacing) {
            return
        }
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        bindUseCases()
    }

    private fun bindUseCases() {
        if (cameraProvider == null) {
            return
        }
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(surfaceProvider)
            }

        val cameraSelector = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        needUpdateGraphicOverlayImageSourceInfo = true

        imageAnalysis.setAnalyzer(
            Executors.newSingleThreadExecutor()
        ) { imageProxy ->
            if (needUpdateGraphicOverlayImageSourceInfo) {
                val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    updateGraphicOverlayImageSourceInfo?.invoke(
                        imageProxy.width,
                        imageProxy.height,
                        isImageFlipped
                    )
                } else {
                    updateGraphicOverlayImageSourceInfo?.invoke(
                        imageProxy.height,
                        imageProxy.width,
                        isImageFlipped
                    )
                }
                needUpdateGraphicOverlayImageSourceInfo = false
            }

            onNextFrameAvailable?.invoke(imageProxy)
        }

        cameraProvider?.unbindAll()
        lifecycleOwner?.let {
            cameraProvider?.bindToLifecycle(
                it, cameraSelector, preview, imageAnalysis
            )
        }
    }

    fun destroy() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        lifecycleOwner = null
        surfaceProvider = null
        onNextFrameAvailable = null
        updateGraphicOverlayImageSourceInfo = null
    }
}