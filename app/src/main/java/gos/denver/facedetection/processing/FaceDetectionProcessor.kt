package gos.denver.facedetection.processing

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build.VERSION_CODES
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.GuardedBy
import androidx.annotation.RequiresApi
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.gms.tasks.Tasks
import com.google.android.odml.image.BitmapMlImageBuilder
import com.google.android.odml.image.ByteBufferMlImageBuilder
import com.google.android.odml.image.MediaMlImageBuilder
import com.google.android.odml.image.MlImage
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import java.lang.Math.max
import java.lang.Math.min
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

interface FaceDetectionProcessor {

    @Throws(MlKitException::class)
    fun processImageProxy(image: ImageProxy)

    fun detectInImage(image: InputImage): Task<List<Face>>

    fun stop()
}


//abstract class VisionProcessorBase<T>(context: Context) : FaceDetectionProcessor {
//
//    companion object {
//        const val MANUAL_TESTING_LOG = "LogTagForTest"
//        private const val TAG = "VisionProcessorBase"
//    }
//
//    private var activityManager: ActivityManager =
//        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//    private val fpsTimer = Timer()
//    private val executor = ScopedExecutor(TaskExecutors.MAIN_THREAD)
//
//    // Whether this processor is already shut down
//    private var isShutdown = false
//
//    // Used to calculate latency, running in the same thread, no sync needed.
//    private var numRuns = 0
//    private var totalFrameMs = 0L
//    private var maxFrameMs = 0L
//    private var minFrameMs = Long.MAX_VALUE
//    private var totalDetectorMs = 0L
//    private var maxDetectorMs = 0L
//    private var minDetectorMs = Long.MAX_VALUE
//
//    // Frame count that have been processed so far in an one second interval to calculate FPS.
//    private var frameProcessedInOneSecondInterval = 0
//    private var framesPerSecond = 0
//
//    // To keep the latest images and its metadata.
//    @GuardedBy("this") private var latestImage: ByteBuffer? = null
//    @GuardedBy("this") private var latestImageMetaData: FrameMetadata? = null
//    // To keep the images and metadata in process.
//    @GuardedBy("this") private var processingImage: ByteBuffer? = null
//    @GuardedBy("this") private var processingMetaData: FrameMetadata? = null
//
//
//
//
//
//    // -----------------Code for processing live preview frame from Camera1 API-----------------------
//
//
//
//
//
//    // -----------------Code for processing live preview frame from CameraX API-----------------------
//    @RequiresApi(VERSION_CODES.LOLLIPOP)
//    @ExperimentalGetImage
//    override fun processImageProxy(image: ImageProxy) {
//        val frameStartMs = SystemClock.elapsedRealtime()
//        if (isShutdown) {
//            return
//        }
//        var bitmap: Bitmap? = null
//        if (!PreferenceUtils.isCameraLiveViewportEnabled(graphicOverlay.context)) {
//            bitmap = BitmapUtils.getBitmap(image)
//        }
//
//        if (isMlImageEnabled(graphicOverlay.context)) {
//            val mlImage =
//                MediaMlImageBuilder(image.image!!).setRotation(image.imageInfo.rotationDegrees).build()
//            requestDetectInImage(
//                mlImage,
//                graphicOverlay,
//                /* originalCameraImage= */ bitmap,
//                /* shouldShowFps= */ true,
//                frameStartMs
//            )
//                // When the image is from CameraX analysis use case, must call image.close() on received
//                // images when finished using them. Otherwise, new images may not be received or the camera
//                // may stall.
//                // Currently MlImage doesn't support ImageProxy directly, so we still need to call
//                // ImageProxy.close() here.
//                .addOnCompleteListener { image.close() }
//
//            return
//        }
//
//        requestDetectInImage(
//            InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees),
//            graphicOverlay,
//            /* originalCameraImage= */ bitmap,
//            /* shouldShowFps= */ true,
//            frameStartMs
//        )
//            // When the image is from CameraX analysis use case, must call image.close() on received
//            // images when finished using them. Otherwise, new images may not be received or the camera
//            // may stall.
//            .addOnCompleteListener { image.close() }
//    }
//
//    // -----------------Common processing logic-------------------------------------------------------
//    private fun requestDetectInImage(
//        image: InputImage,
//        graphicOverlay: GraphicOverlay,
//        originalCameraImage: Bitmap?,
//        shouldShowFps: Boolean,
//        frameStartMs: Long
//    ): Task<T> {
//        return setUpListener(
//            detectInImage(image),
//            graphicOverlay,
//            originalCameraImage,
//            shouldShowFps,
//            frameStartMs
//        )
//    }
//
//    private fun requestDetectInImage(
//        image: MlImage,
//        graphicOverlay: GraphicOverlay,
//        originalCameraImage: Bitmap?,
//        shouldShowFps: Boolean,
//        frameStartMs: Long
//    ): Task<T> {
//        return setUpListener(
//            detectInImage(image),
//            graphicOverlay,
//            originalCameraImage,
//            shouldShowFps,
//            frameStartMs
//        )
//    }
//
//    private fun setUpListener(
//        task: Task<T>,
//        graphicOverlay: GraphicOverlay,
//        originalCameraImage: Bitmap?,
//        shouldShowFps: Boolean,
//        frameStartMs: Long
//    ): Task<T> {
//        val detectorStartMs = SystemClock.elapsedRealtime()
//        return task
//            .addOnSuccessListener(
//                executor,
//                OnSuccessListener { results: T ->
//                    val endMs = SystemClock.elapsedRealtime()
//                    val currentFrameLatencyMs = endMs - frameStartMs
//                    val currentDetectorLatencyMs = endMs - detectorStartMs
//                    if (numRuns >= 500) {
//                        resetLatencyStats()
//                    }
//                    numRuns++
//                    frameProcessedInOneSecondInterval++
//                    totalFrameMs += currentFrameLatencyMs
//                    maxFrameMs = max(currentFrameLatencyMs, maxFrameMs)
//                    minFrameMs = min(currentFrameLatencyMs, minFrameMs)
//                    totalDetectorMs += currentDetectorLatencyMs
//                    maxDetectorMs = max(currentDetectorLatencyMs, maxDetectorMs)
//                    minDetectorMs = min(currentDetectorLatencyMs, minDetectorMs)
//
//                    // Only log inference info once per second. When frameProcessedInOneSecondInterval is
//                    // equal to 1, it means this is the first frame processed during the current second.
//                    if (frameProcessedInOneSecondInterval == 1) {
//                        Log.d(TAG, "Num of Runs: $numRuns")
//                        Log.d(
//                            TAG,
//                            "Frame latency: max=" +
//                                    maxFrameMs +
//                                    ", min=" +
//                                    minFrameMs +
//                                    ", avg=" +
//                                    totalFrameMs / numRuns
//                        )
//                        Log.d(
//                            TAG,
//                            "Detector latency: max=" +
//                                    maxDetectorMs +
//                                    ", min=" +
//                                    minDetectorMs +
//                                    ", avg=" +
//                                    totalDetectorMs / numRuns
//                        )
//                        val mi = ActivityManager.MemoryInfo()
//                        activityManager.getMemoryInfo(mi)
//                        val availableMegs: Long = mi.availMem / 0x100000L
//                        Log.d(TAG, "Memory available in system: $availableMegs MB")
//                    }
//                    graphicOverlay.clear()
//                    if (originalCameraImage != null) {
//                        graphicOverlay.add(CameraImageGraphic(graphicOverlay, originalCameraImage))
//                    }
//                    this@VisionProcessorBase.onSuccess(results, graphicOverlay)
//                    if (!PreferenceUtils.shouldHideDetectionInfo(graphicOverlay.context)) {
//                        graphicOverlay.add(
//                            InferenceInfoGraphic(
//                                graphicOverlay,
//                                currentFrameLatencyMs,
//                                currentDetectorLatencyMs,
//                                if (shouldShowFps) framesPerSecond else null
//                            )
//                        )
//                    }
//                    graphicOverlay.postInvalidate()
//                }
//            )
//            .addOnFailureListener(
//                executor,
//                OnFailureListener { e: Exception ->
//                    graphicOverlay.clear()
//                    graphicOverlay.postInvalidate()
//                    val error = "Failed to process. Error: " + e.localizedMessage
//                    Toast.makeText(
//                        graphicOverlay.context,
//                        """
//          $error
//          Cause: ${e.cause}
//          """.trimIndent(),
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                    Log.d(TAG, error)
//                    e.printStackTrace()
//                    this@VisionProcessorBase.onFailure(e)
//                }
//            )
//    }
//
//    override fun stop() {
//        executor.shutdown()
//        isShutdown = true
//        resetLatencyStats()
//        fpsTimer.cancel()
//    }
//
//    private fun resetLatencyStats() {
//        numRuns = 0
//        totalFrameMs = 0
//        maxFrameMs = 0
//        minFrameMs = Long.MAX_VALUE
//        totalDetectorMs = 0
//        maxDetectorMs = 0
//        minDetectorMs = Long.MAX_VALUE
//    }
//
//    protected abstract fun detectInImage(image: InputImage): Task<T>
//
//    protected open fun detectInImage(image: MlImage): Task<T> {
//        return Tasks.forException(
//            MlKitException(
//                "MlImage is currently not demonstrated for this feature",
//                MlKitException.INVALID_ARGUMENT
//            )
//        )
//    }
//
//    protected abstract fun onSuccess(results: T, graphicOverlay: GraphicOverlay)
//
//    protected abstract fun onFailure(e: Exception)
//
//    protected open fun isMlImageEnabled(context: Context?): Boolean {
//        return false
//    }
//}

class ScopedExecutor(private val executor: Executor) : Executor {
    private val shutdown = AtomicBoolean()

    override fun execute(command: Runnable) {
        // Return early if this object has been shut down.
        if (shutdown.get()) {
            return
        }
        executor.execute {
            // Check again in case it has been shut down in the mean time.
            if (shutdown.get()) {
                return@execute
            }
            command.run()
        }
    }

    /**
     * After this method is called, no runnables that have been submitted or are subsequently
     * submitted will start to execute, turning this executor into a no-op.
     *
     *
     * Runnables that have already started to execute will continue.
     */
    fun shutdown() {
        shutdown.set(true)
    }
}