package gos.denver.facedetection.ui

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import gos.denver.facedetection.domain.FaceDetectionStore
import gos.denver.facedetection.domain.FaceDetectionStoreFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class FaceDetectionViewModel : ViewModel() {

    private val faceDetectionStore: FaceDetectionStore = FaceDetectionStoreFactory(DefaultStoreFactory()).createStore()


    val uiStates = faceDetectionStore
        .states
    // todo linreal: mapper to UIState!!
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = faceDetectionStore.state
        )


    fun onNextFrameAvailable(imageProxy: ImageProxy) {
        viewModelScope.launch {
            faceDetectionStore.accept(FaceDetectionStore.Intent.OnNextFrame(imageProxy))
        }
    }

    fun switchCameraFace() {

        faceDetectionStore.accept(FaceDetectionStore.Intent.SwitchCameraFace)
    }

    override fun onCleared() {
        faceDetectionStore.dispose()
        super.onCleared()
    }
}
