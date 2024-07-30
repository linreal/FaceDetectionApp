package gos.denver.facedetection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.face.Face
import gos.denver.facedetection.databinding.FragmentFaceDetectionBinding
import gos.denver.facedetection.ui.CameraManager
import gos.denver.facedetection.ui.FaceDetectionViewModel
import gos.denver.facedetection.ui.FaceRectangle
import gos.denver.facedetection.ui.UIState
import kotlinx.coroutines.launch

class FaceDetectionFragment : Fragment() {
    companion object {
        fun newInstance() = FaceDetectionFragment()
    }

    private var _binding: FragmentFaceDetectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FaceDetectionViewModel by viewModels()
    private val cameraManager: CameraManager = CameraManager()

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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStates.collect { state ->
                    updateUI(state)
                }
            }
        }

        binding.cameraSwitchButton.setOnClickListener {
            viewModel.switchCameraFace()
        }
    }

    private fun updateUI(state: UIState) {
        if (state.cameraProvider != null) {
            cameraManager.init(
                state.cameraProvider,
                viewLifecycleOwner,
                binding.cameraPreview.surfaceProvider,
                viewModel::onNextFrameAvailable,
                binding.faceOverlayView::setImageSourceInfo,
                state.cameraLensFacing
            )
        }
        updateFaceOverlay(state.faces)
        cameraManager.switchCamera(state.cameraLensFacing)
    }

    private fun updateFaceOverlay(faces: List<Face>) {
        binding.faceOverlayView.clear()
        faces.forEach { face ->
            binding.faceOverlayView.add(
                FaceRectangle(
                    face,
                    binding.faceOverlayView.scaleFactor,
                    binding.faceOverlayView.postScaleWidthOffset,
                    binding.faceOverlayView.postScaleHeightOffset,
                    binding.faceOverlayView.width,
                    binding.faceOverlayView.isImageFlipped
                )
            )
        }
    }

    override fun onDestroyView() {
        cameraManager.destroy()
        _binding = null
        super.onDestroyView()
    }
}