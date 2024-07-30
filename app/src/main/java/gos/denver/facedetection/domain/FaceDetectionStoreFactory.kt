package gos.denver.facedetection.domain

import android.util.Log
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

internal class FaceDetectionStoreFactory(
    private val storeFactory: StoreFactory,
) {

    fun createStore(): FaceDetectionStore {
        Log.d("AdsTag", "createStore") // todo linreal: remove log
        val name = FaceDetectionStore::class.qualifiedName
        val initialState = FaceDetectionStore.State(
            faces = emptyList(), cameraFace = FaceDetectionStore.CameraFace.FRONT
        )

        return object : FaceDetectionStore,
            Store<FaceDetectionStore.Intent, FaceDetectionStore.State, FaceDetectionStore.Label> by storeFactory.create(
                name = name,
                initialState = initialState,
                bootstrapper = SimpleBootstrapper(),
                executorFactory = {
                    FaceDetectionStoreExecutor()
                },
                reducer = FaceDetectionStoreReducer,
            ) {}
    }
}