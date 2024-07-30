package gos.denver.facedetection.domain

import android.graphics.Rect
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.launch


internal class FaceDetectionStoreExecutor :
    CoroutineExecutor<FaceDetectionStore.Intent, FaceDetectionStore.Action, FaceDetectionStore.State, FaceDetectionStore.Message, FaceDetectionStore.Label>() {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .build()
    )

    override fun executeIntent(intent: FaceDetectionStore.Intent) {
        when (intent) {
            is FaceDetectionStore.Intent.OnNextFrame -> analyzeFrame(intent.imageProxy)
            is FaceDetectionStore.Intent.SwitchCameraFace -> {
               // todo linreal:
            }
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                //Log.d("AdsTag", "analyzeFrame: ${mediaImage.width} ${mediaImage.height}")
            detector.process(image)
                .addOnSuccessListener { faces ->

                    val rects = faces.map {
                        it.boundingBox
                    }

                    scope.launch {
                        dispatch(FaceDetectionStore.Message.OnFacesDetected(faces))
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}