package gos.denver.facedetection.ui

import android.app.Application
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.google.mlkit.vision.face.Face
import gos.denver.facedetection.domain.FaceDetectionStore
import gos.denver.facedetection.domain.FaceDetectionStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class UIState(
    val faces: List<Face>,
    val cameraLensFacing: Int,
    val cameraProvider: ProcessCameraProvider?
)

class FaceDetectionViewModel(private val appContext: Application) : AndroidViewModel(appContext) {

    private val faceDetectionStore: FaceDetectionStore =
        FaceDetectionStoreFactory(DefaultStoreFactory()).createStore()

    private val cameraProviderFlow: Flow<ProcessCameraProvider> = flow {
        val cameraProvider = getCameraProvider()
        emit(cameraProvider)
    }

    val uiStates: StateFlow<UIState> =
        combine(
            faceDetectionStore.states,
            cameraProviderFlow
        ) { storeState, cameraProvider ->
            storeState.toUIState(cameraProvider)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = faceDetectionStore.state.toUIState(null)
        )


    fun onNextFrameAvailable(imageProxy: ImageProxy) {
        viewModelScope.launch {
            faceDetectionStore.accept(FaceDetectionStore.Intent.OnNextFrame(imageProxy))
        }
    }


    fun switchCameraFace() {
        faceDetectionStore.accept(FaceDetectionStore.Intent.SwitchCameraFace)
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(appContext)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    continuation.resume(cameraProvider)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(appContext))
        }

    override fun onCleared() {
        faceDetectionStore.dispose()
        super.onCleared()
    }

}

private fun FaceDetectionStore.State.toUIState(cameraProvider: ProcessCameraProvider?): UIState {
    return UIState(
        faces = faces,
        cameraLensFacing = when (cameraFace) {
            FaceDetectionStore.CameraFace.FRONT -> CameraSelector.LENS_FACING_FRONT
            FaceDetectionStore.CameraFace.BACK -> CameraSelector.LENS_FACING_BACK
        },
        cameraProvider = cameraProvider
    )
}

