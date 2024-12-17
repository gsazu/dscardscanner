package com.ds.cardscanner.service

import android.app.Activity
import android.content.Intent
import com.ds.cardscanner.DsLiveScannerActivity
import com.ds.cardscanner.DsScannerActivity
import com.ds.cardscanner.DsScannerViewModel
import com.ds.cardscanner.init.DsScannerFactory

class DsScannerService(private val activity: Activity) {

    fun openScanner(
        listener: DsScannerFactory.OnCardScanResponseListener
    ) {
        DsScannerViewModel().getInstance().setCardResponseListener(listener)
        activity.startActivity(Intent(activity, DsLiveScannerActivity::class.java))
    }
}