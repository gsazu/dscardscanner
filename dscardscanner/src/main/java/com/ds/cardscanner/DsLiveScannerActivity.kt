package com.ds.cardscanner

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.ds.cardscanner.databinding.ActivityDsLiveScannerBinding
import com.ds.cardscanner.models.CardDetails
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DsLiveScannerActivity : AppCompatActivity(), ImageAnalysis.Analyzer {
    private lateinit var binding: ActivityDsLiveScannerBinding
    private val TAG = "DsLiveScannerActivity"
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private lateinit var cameraExecutor: ExecutorService
    private var totalString: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDsLiveScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        activityResultLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA
            )
        )

    }

    private val activityResultLauncher =
        this.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in arrayOf(
                        Manifest.permission.CAMERA
                    ) && it.value == false
                )
                    permissionGranted = false
            }
            if (!permissionGranted) {
//                Toast.makeText(baseContext,
//                    "Permission request denied",
//                    Toast.LENGTH_SHORT).show()
            } else {
                startCameraBg()
                startCamera()
            }
        }

    private fun startCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraView2.surfaceProvider)
                }
            val imageAnalyser = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                    it.setAnalyzer(cameraExecutor, this)
                }
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyser)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startCameraBg() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraView.surfaceProvider)
                }
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private var lastAnalyzedTimestamp = 0L
    private val frameDelayMillis = 2000 // Adjust this value to control the delay

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp < frameDelayMillis) {
            image.close()
            return
        }
        lastAnalyzedTimestamp = currentTimestamp

        val mediaImage = image.image
        val inputImage = mediaImage?.let { InputImage.fromMediaImage(it, image.imageInfo.rotationDegrees) }

        inputImage?.let {
            recognizer.process(it)
                .addOnSuccessListener { visionText ->
                    // Process recognized text
//                    Log.d(TAG, "Detected text: ${visionText.text}")
//                    binding.tvResult.text = visionText.text
                    fetchData(visionText.text)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Text recognition failed: ${e.message}")
                }
                .addOnCompleteListener {
                    image.close()
                }
        } ?: run {
            image.close()
        }
    }

    private fun fetchData(text: String) {
        if (text.isNotEmpty()) {
            val textList = text.split("\n")
            Log.d(TAG, "onCreate: ${textList}")
            if(DsUtils.cardNumber.isEmpty()){
                DsUtils.getCardNumberFromList(textList)
            }

            if (DsUtils.cardExpiry.isEmpty()) {
                DsUtils.getExpiryDateFromList(textList).isNotEmpty()
            }

            for (i in textList) {
//                        if (DsUtils.cardExpiry.isEmpty()) {
//                            if (DsUtils.getExpiryDate(i).isNotEmpty()) {
//                                totalString += "\n" + DsUtils.getExpiryDate(i)
//                            }
//                        }
                if (DsUtils.cardCvv.isEmpty()) {
                    if (DsUtils.getCvv(i).isNotEmpty()) {
                        totalString += "\n" + DsUtils.cardCvv
                    }
                }
                if (DsUtils.cardName.isEmpty()) {
                    if (DsUtils.getCardName(i).isNotEmpty()) {
                        totalString += "\n" + DsUtils.getCardName(i)
                    }
                }
            }

            Log.d("cardCvv", "onCreate: ${DsUtils.cardCvv}")

            Handler(Looper.getMainLooper()).postDelayed({
                if (DsUtils.cardName.isEmpty()) {
                    DsScannerActivity.isFirstSideScanned = false
                } else {
                    if (DsUtils.cardExpiry.isEmpty() && DsUtils.cardNumber.isEmpty()) {
                        DsScannerActivity.isFirstSideScanned = true
                        binding.tvScanSide.text = "Scan Back Side"
                    } else if (DsUtils.cardExpiry.isNotEmpty() && DsUtils.cardNumber.isNotEmpty()) {
                        DsScannerActivity.isFirstSideScanned = true
                        binding.tvScanSide.text = "Scan Back Side"
                    } else {
                        if(!DsScannerActivity.isFirstSideScanned) {
                            DsScannerActivity.isFirstSideScanned = false
                        }
                    }
                }

                if(DsUtils.cardName.isNotEmpty() && DsUtils.cardNumber.isNotEmpty() &&
                    DsUtils.cardCvv.isNotEmpty() && DsUtils.cardExpiry.isNotEmpty()){
                    DsScannerActivity.isFirstSideScanned = true
                    DsScannerActivity.isSecondSideScanned = true
                    binding.tvScanSide.text = "Scan Back Side"
                    val cardDetails = CardDetails(DsUtils.cardNumber,DsUtils.cardName,
                        DsUtils.cardCvv, DsUtils.cardExpiry)
                    DsScannerViewModel().getInstance().setCardResponse(cardDetails)
                    allClear()
                    finish()
                }

//                if (!DsScannerActivity.isFirstSideScanned) {
//                    btn_scanAgain.visibility = View.VISIBLE
//                } else {
//                    btn_scanAgain.visibility = View.GONE
//                    btn_scanBackSide.visibility = View.VISIBLE
//                }
//
//                if(DsScannerActivity.isSecondSideScanned) {
//                    btn_scanBackSide.setText("Done")
//                }
//
//                tv_result.text = "CardName: ${DsUtils.cardName}\n" +
//                        "Card Number: ${DsUtils.cardNumber}\n" +
//                        "Card Expiry: ${DsUtils.cardExpiry}\n" +
//                        "Card CVV: ${DsUtils.cardCvv}"
            }, 2000)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val cardDetails = CardDetails()
        DsScannerViewModel().getInstance().setCardResponse(cardDetails)
        allClear()
    }

    private fun allClear(){
        DsUtils.cardName = ""
        DsUtils.cardExpiry = ""
        DsUtils.cardNumber = ""
        DsUtils.cardCvv = ""
        DsUtils.expOne = ""
        DsUtils.countForExp = 0
        DsUtils.clearCache(this)
    }
}