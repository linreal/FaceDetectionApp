package gos.denver.facedetection

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.face.Face
import gos.denver.facedetection.databinding.FragmentFaceDetectionBinding
import gos.denver.facedetection.ui.FaceDetectionViewModel
import gos.denver.facedetection.ui.FaceGraphic
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class FaceDetectionFragment : Fragment() {
    companion object {
        fun newInstance() = FaceDetectionFragment()
    }

    private var _binding: FragmentFaceDetectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FaceDetectionViewModel by viewModels() // todo linreal: rename

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceDetectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCamera()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStates.collect { state ->
                    updateFaceOverlay(state.faces)
                    // todo linreal: implement
                   // Log.d("AdsTag", "state: $state")
                }
            }
        }
    }
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            needUpdateGraphicOverlayImageSourceInfo = true

            imageAnalyzer.setAnalyzer(
                // todo linreal: newSingleThreadExecutor? not sure
                Executors.newSingleThreadExecutor()
            ) { imageProxy ->

                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        binding.faceOverlayView.setImageSourceInfo(imageProxy.width, imageProxy.height, isImageFlipped)
                    } else {
                        binding.faceOverlayView.setImageSourceInfo(imageProxy.height, imageProxy.width, isImageFlipped)
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }

                viewModel.onNextFrameAvailable(imageProxy)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e("AdsTag", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun updateFaceOverlay(faces: List<Face>) {
        binding.faceOverlayView.clear()
        if (faces.isEmpty()) return
        faces.forEach {
            binding.faceOverlayView.add(FaceGraphic(binding.faceOverlayView, it))
        }
    }

    override fun onResume() {
        // todo linreal: do smth more clever with it?
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}