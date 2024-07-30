package gos.denver.facedetection.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

internal class FaceDetectionStoreFactory(
    private val storeFactory: StoreFactory,
) {

    fun createStore(): FaceDetectionStore {
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