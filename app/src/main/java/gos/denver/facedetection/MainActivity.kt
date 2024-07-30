package gos.denver.facedetection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
            if (savedInstanceState == null) {
                startFaceDetectionFragment()
            }
        } else {
            tryRequestPermission()
        }
    }

    private fun tryRequestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.camera_permission_alert_title)
                .setMessage(R.string.camera_permission_alert_text)
                .setPositiveButton(R.string.camera_permission_alert_yes) { _, _ ->
                    requestCameraPermission()
                }
                .show()
        } else {
            requestCameraPermission()
        }
    }

    private fun startFaceDetectionFragment() {
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
                Toast.makeText(this, R.string.camera_permission_fail, Toast.LENGTH_SHORT).show()
           }
        }

    private companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }
}