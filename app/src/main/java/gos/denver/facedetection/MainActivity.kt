package gos.denver.facedetection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import gos.denver.facedetection.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (allPermissionsGranted()) {
            startFaceDetectionFragment()
        } else {
            tryRequestPermission()
        }
    }

    private fun tryRequestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION)) {
            AlertDialog.Builder(this) // todo linreal: strings
                .setTitle("I want permission permission!")
                .setMessage("Give me permission to use the camera, or else")
                .setPositiveButton("Sure") { _, _ ->
                    requestCameraPermission()
                }
                .show()
        } else {
            requestCameraPermission()
        }
    }

    private fun startFaceDetectionFragment() {
        Log.d("AdsTag", "startCameraFragment")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FaceDetectionFragment.newInstance())
            .commit()
    }

    private fun requestCameraPermission() {
        cameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext,
        CAMERA_PERMISSION
    ) == PackageManager.PERMISSION_GRANTED

    private val cameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
           if (permission) {
                startFaceDetectionFragment()
              } else {
                  // todo linreal: move to strings
                Toast.makeText(this, "No camera for you then, sorry", Toast.LENGTH_SHORT).show()
           }
        }

    companion object {
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }
}