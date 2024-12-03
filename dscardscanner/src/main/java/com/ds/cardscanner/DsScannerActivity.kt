package com.ds.cardscanner

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ds.cardscanner.models.CardDetails
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DsScannerActivity : AppCompatActivity() {

    private var camera: Camera? = null
    private lateinit var surfaceView: SurfaceView
    private lateinit var captureButton: View
    private lateinit var tv_header: TextView
    private var surfaceHolder: SurfaceHolder? = null
    private var surfaceHolderMain: SurfaceHolder? = null
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private var inputImage:InputImage ? = null

    companion object{
        var croppedBitmap : Bitmap? = null
        var isFirstSideScanned = false
        var isSecondSideScanned = false
        var headerText: String = "Scan First Side"
    }

    private var permissions = arrayOf(Manifest.permission.CAMERA)
    private val TAG = "DsScannerActivity"

    val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableClassification()  // Optional
        .build()

    val objectDetector = ObjectDetection.getClient(options)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ds_scanner)
        init()
        checkPermission(permissions)
    }

    private fun init(){
        surfaceView = findViewById(R.id.surfaceView)
        captureButton = findViewById(R.id.view_capture)
        tv_header = findViewById(R.id.tv_header)

        tv_header.text = headerText
        // SurfaceView setup
        surfaceHolder = surfaceView.holder
        surfaceHolder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                // Open the camera and start the preview
                openCamera(p0)
            }

            override fun surfaceChanged(p0: SurfaceHolder, format: Int, width: Int, height: Int) {
                // Adjust camera preview when surface size changes
                camera?.setPreviewDisplay(p0)
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                // Release the camera when the surface is destroyed
                releaseCamera()
            }
        })


        // Capture button logic
        captureButton.setOnClickListener {
            captureImage()
        }
    }

    private fun checkPermission(permission: Array<String>) {
        for (i in permission.indices) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission[i]
                ) == PackageManager.PERMISSION_DENIED
            ) {
                // Requesting the permission
                ActivityCompat.requestPermissions(this, permission, 5000)
            } else {
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 5000 && resultCode == RESULT_OK) {
        }
    }

    private fun openCamera(holder: SurfaceHolder?) {
        try {
            camera = Camera.open(0)

            val params = camera?.parameters
            val supportedFocusModes = params?.supportedFocusModes
            if (supportedFocusModes?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) == true) {
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            }

            camera?.parameters = params

            // Set the preview display
            camera?.setPreviewDisplay(holder)

            // Set orientation and start preview
            val rotation = windowManager.defaultDisplay.rotation
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(0, cameraInfo)
            val orientation = when (rotation) {
                Surface.ROTATION_0 -> cameraInfo.orientation
                Surface.ROTATION_90 -> (cameraInfo.orientation + 90) % 360
                Surface.ROTATION_180 -> (cameraInfo.orientation + 180) % 360
                Surface.ROTATION_270 -> (cameraInfo.orientation + 270) % 360
                else -> 0
            }
            camera?.setDisplayOrientation(orientation)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show()
        }
    }


    // Capture the image
    private fun captureImage() {
        camera?.takePicture(null, null, Camera.PictureCallback { data, _ ->
            // Convert the captured byte array to a Bitmap
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)


            inputImage = InputImage.fromBitmap(bitmap, 0)

            objectDetector.process(inputImage!!)
                .addOnSuccessListener { detectedObjects ->
                    // Task completed successfully
                    // ...
                    if(detectedObjects.isNotEmpty()) {
                        croppedBitmap = showCroppedImage(bitmap,detectedObjects.first().boundingBox)
                        startActivity(Intent(this, PreviewActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "captureImage: Error ${e.message}")
                }

            // Save the image to local storage
            val photoFile = createImageFile()

            try {
                val outputStream = FileOutputStream(photoFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                // Show a message indicating that the image has been saved
                Toast.makeText(this, "Image saved at ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()

                Log.d("CameraApp", "Image saved at: ${photoFile.absolutePath}")

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Create a unique filename for the captured image
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())
        val storageDir = getExternalFilesDir(null) ?: filesDir
        return File(storageDir, "JPEG_${timestamp}.jpg")
    }

    // Release the camera
    private fun releaseCamera() {
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    override fun onPause() {
        super.onPause()
        releaseCamera()
    }

    fun showCroppedImage(sourceBitmap: Bitmap, rect: Rect) : Bitmap{
        // Ensure the Rect is within the bounds of the source bitmap
        val width = rect.width()
        val height = rect.height()

        // Check if the Rect is within the bounds of the source Bitmap
        if (rect.left < 0 || rect.top < 0 || rect.right > sourceBitmap.width || rect.bottom > sourceBitmap.height) {
            throw IllegalArgumentException("Rect is outside the bounds of the source bitmap")
        }

        // Crop the bitmap based on the rect
        val croppedBitmap = Bitmap.createBitmap(sourceBitmap, rect.left, rect.top, width, height)

        // Set the cropped bitmap to the ImageView
        return croppedBitmap
    }

    override fun onBackPressed() {
        super.onBackPressed()
        DsUtils.cardName = ""
        DsUtils.cardExpiry = ""
        DsUtils.cardNumber = ""
        DsUtils.cardCvv = ""

        DsScannerActivity.croppedBitmap = null
        val cardDetails = CardDetails()
        DsScannerViewModel().getInstance().setCardResponse(cardDetails)

        DsUtils.clearCache(applicationContext)
        finish()
    }
}