package gos.denver.facedetection.domain

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


internal class FaceDetectionStoreExecutor :
    CoroutineExecutor<FaceDetectionStore.Intent, FaceDetectionStore.Action, FaceDetectionStore.State, FaceDetectionStore.Message, FaceDetectionStore.Label>() {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .build()
    )

    override fun executeIntent(intent: FaceDetectionStore.Intent) {
        when (intent) {
            is FaceDetectionStore.Intent.OnNextFrame -> analyzeFrame(intent.imageProxy)
            is FaceDetectionStore.Intent.SwitchCameraFace -> switchCameraFace()
        }
    }

    private fun switchCameraFace() {
        scope.launch {
            val newCameraFace = when (state().cameraFace) {
                FaceDetectionStore.CameraFace.FRONT -> FaceDetectionStore.CameraFace.BACK
                FaceDetectionStore.CameraFace.BACK -> FaceDetectionStore.CameraFace.FRONT
            }
            dispatch(FaceDetectionStore.Message.CameraFaceSwitched(newCameraFace))
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeFrame(imageProxy: ImageProxy) {
        scope.launch {
            withContext(Dispatchers.IO) {
                // todo: move processing to separate class
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image =
                        InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    detector.process(image)
                        .addOnSuccessListener { faces ->
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
    }

    override fun dispose() {
        detector.close()
        super.dispose()
    }
}