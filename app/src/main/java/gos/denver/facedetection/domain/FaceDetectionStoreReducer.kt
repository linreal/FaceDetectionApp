package gos.denver.facedetection.domain

import com.arkivanov.mvikotlin.core.store.Reducer

internal object FaceDetectionStoreReducer :
    Reducer<FaceDetectionStore.State, FaceDetectionStore.Message> {

    override fun FaceDetectionStore.State.reduce(msg: FaceDetectionStore.Message): FaceDetectionStore.State {
        return when (msg) {
            is FaceDetectionStore.Message.OnFacesDetected -> copy(
                faces = msg.faces,
            )
            is FaceDetectionStore.Message.CameraFaceSwitched -> copy(
                cameraFace = msg.cameraFace,
            )
        }
    }
}