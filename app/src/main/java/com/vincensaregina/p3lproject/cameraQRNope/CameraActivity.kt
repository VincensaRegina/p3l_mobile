package com.vincensaregina.p3lproject.cameraQRNope

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.vincensaregina.p3lproject.MainActivity
import com.vincensaregina.p3lproject.R
import java.util.concurrent.ExecutionException


class CameraActivity : AppCompatActivity() {
    //Camera
    private val PERMISSION_REQUEST_CAMERA: Int = 0
    private lateinit var previewView: PreviewView
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private lateinit var qrCodeFoundButton: Button
    private lateinit var qrCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.activity_camera_previewView)

        qrCodeFoundButton = findViewById(R.id.activity_main_qrCodeFoundButton)
        qrCodeFoundButton.visibility = View.INVISIBLE
        qrCodeFoundButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Toast.makeText(applicationContext, qrCode, Toast.LENGTH_SHORT).show()
                Log.i(MainActivity::class.java.simpleName, "QR Code Found: $qrCode")
            }
        })

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        requestCamera()
    }

    private fun requestCamera() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@CameraActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CAMERA
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CAMERA
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {
        //to initialize the preview from the camera using a ProcessCameraProvider.
        cameraProviderFuture.addListener({
            try {
                val cameraProvider =
                    cameraProviderFuture.get()
                bindCameraPreview(cameraProvider, qrCode)
            } catch (e: ExecutionException) {
                Toast.makeText(
                    this,
                    "Error starting camera " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: InterruptedException) {
                Toast.makeText(
                    this,
                    "Error starting camera " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraPreview(cameraProvider: ProcessCameraProvider, qrCode: String) {
        //to set up and initiate the camera preview to be displayed inside the PreviewView widget.
        previewView.preferredImplementationMode = PreviewView.ImplementationMode.SURFACE_VIEW
        //create a new CameraX Preview object using the builder. We will then create a new CameraX CameraSelector object using the
        // builder using the back lens of the camera.
        val preview = Preview.Builder()
            .build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        //to set the surface provider as a newly created surface provider from the PreviewView widget using the setSurfaceProvider(…) method
        preview.setSurfaceProvider(previewView.createSurfaceProvider())

        //create a new ImageAnalysis object from the CameraX library using the builder.
        // Then we will use the setAnaylzer(…) method on the ImageAnalysis object and provide an object of the custom image analyzer
        // class we created in a previous step.
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(this),
            QRCodeImageAnalyzer(object : QRCodeFoundListener {
                //These methods will be responsible for storing the text retrieved from the QR code in
                // the activity class along with updating the visibility status of the button depending
                // on whether a code has been located or note.
                override fun onQRCodeFound(_qrCode: String) {
//                    qrCode = _qrCode.toString()
                    qrCodeFoundButton.visibility = View.VISIBLE
                }

                override fun qrCodeNotFound() {
                    qrCodeFoundButton.visibility = View.INVISIBLE
                }
            })
        )

        val camera = cameraProvider.bindToLifecycle(
            (this as LifecycleOwner),
            cameraSelector,
            imageAnalysis,
            preview
        )
    }
}