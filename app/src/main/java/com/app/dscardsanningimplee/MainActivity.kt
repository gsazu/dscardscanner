package com.app.dscardsanningimplee

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.dscardsanningimplee.databinding.ActivityMainBinding
import com.ds.cardscanner.DsScannerActivity
import com.ds.cardscanner.DsUtils
import com.ds.cardscanner.init.DsScannerFactory
import com.ds.cardscanner.models.CardDetails

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.tvHelloWorld.setOnClickListener {
            DsScannerFactory.create(this).openScanner(object: DsScannerFactory.OnCardScanResponseListener {
                override fun cardScanResponse(cardDetails: CardDetails) {
                    binding.tvHelloWorld.text = "Card Name: ${cardDetails.cardName}\n" +
                            "Card Number: ${cardDetails.cardNumber}\n" +
                            "Card Expiry: ${cardDetails.cardExpiry}\n" +
                            "Card CVV: ${cardDetails.cardCvv}"
                }

            })
        }

    }
}