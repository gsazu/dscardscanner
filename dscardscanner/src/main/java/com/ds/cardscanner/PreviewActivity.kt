package com.ds.cardscanner

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ds.cardscanner.models.CardDetails
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class PreviewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var tv_result: TextView
    private lateinit var btn_scanAgain: Button
    private lateinit var btn_scanBackSide: Button
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val TAG = "PreviewActivity"
    private var totalString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        imageView = findViewById(R.id.imageView)
        tv_result = findViewById(R.id.tv_result)
        btn_scanAgain = findViewById(R.id.btn_scanAgain)
        btn_scanBackSide = findViewById(R.id.btn_scanBackSide)
        val bb = rotateBitmap(DsScannerActivity.croppedBitmap!!, 90f)
        imageView.setImageBitmap(bb)
        val image = InputImage.fromBitmap(bb, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                // ...
                if (visionText.text.isNotEmpty()) {
                    val textList = visionText.text.split("\n")
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
                            } else if (DsUtils.cardExpiry.isNotEmpty() && DsUtils.cardNumber.isNotEmpty()) {
                                DsScannerActivity.isFirstSideScanned = true
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
                        }

                        if (!DsScannerActivity.isFirstSideScanned) {
                            btn_scanAgain.visibility = View.VISIBLE
                        } else {
                            btn_scanAgain.visibility = View.GONE
                            btn_scanBackSide.visibility = View.VISIBLE
                        }

                        if(DsScannerActivity.isSecondSideScanned) {
                            btn_scanBackSide.setText("Done")
                        }

                        tv_result.text = "CardName: ${DsUtils.cardName}\n" +
                                "Card Number: ${DsUtils.cardNumber}\n" +
                                "Card Expiry: ${DsUtils.cardExpiry}\n" +
                                "Card CVV: ${DsUtils.cardCvv}"
                    }, 2000)
                }
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "onCreateError : ${e.message}")
            }


        btn_scanAgain.setOnClickListener {
            onBackPressed()
        }

        btn_scanBackSide.setOnClickListener {
            onBackPressed()
        }
    }

    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle) // Set the rotation angle
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    override fun onBackPressed() {
        val cardDetails = CardDetails()
        cardDetails.cardCvv = DsUtils.cardCvv
        cardDetails.cardName = DsUtils.cardName
        cardDetails.cardExpiry = DsUtils.cardExpiry
        cardDetails.cardNumber = DsUtils.cardNumber
        DsScannerViewModel().getInstance().setCardResponse(cardDetails)
        if (!DsScannerActivity.isFirstSideScanned) {
            super.onBackPressed()
            DsUtils.cardName = ""
            DsUtils.cardExpiry = ""
            DsUtils.cardNumber = ""
            DsUtils.cardCvv = ""
            DsUtils.countForExp = 0
            DsScannerActivity.croppedBitmap = null
            startActivity(Intent(this, DsScannerActivity::class.java))
            finish()
        } else {
            DsScannerActivity.headerText = "Scan Back Side"
            if(DsScannerActivity.isSecondSideScanned){
                DsUtils.cardName = ""
                DsUtils.cardExpiry = ""
                DsUtils.cardNumber = ""
                DsUtils.cardCvv = ""
                DsUtils.countForExp = 0
                DsUtils.expOne = ""
                DsScannerActivity.headerText = "Scan First Side"
                DsUtils.clearCache(applicationContext)
                DsScannerActivity.isFirstSideScanned = false
                DsScannerActivity.isSecondSideScanned = false
                finish()
            } else {
                DsScannerActivity.croppedBitmap = null
                startActivity(Intent(this, DsScannerActivity::class.java))
                finish()
            }
        }
    }
}