package gos.denver.facedetection.domain

import androidx.camera.core.ImageProxy
import com.arkivanov.mvikotlin.core.store.Store
import com.google.mlkit.vision.face.Face

interface FaceDetectionStore :
    Store<FaceDetectionStore.Intent, FaceDetectionStore.State, FaceDetectionStore.Label> {

    enum class CameraFace {
        FRONT,
        BACK
    }

    data class State(
        val faces: List<Face>,
        val cameraFace: CameraFace
    )

    sealed interface Action

    sealed interface Intent {
        data object SwitchCameraFace : Intent
        data class OnNextFrame(val imageProxy: ImageProxy) : Intent
    }

    sealed interface Message {
        data class CameraFaceSwitched(val cameraFace: CameraFace) : Message
        data class OnFacesDetected(val faces: List<Face>) : Message
    }

    sealed interface Label
}