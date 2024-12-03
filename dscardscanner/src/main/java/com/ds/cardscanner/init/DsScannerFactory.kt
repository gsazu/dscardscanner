package com.ds.cardscanner.init

import android.app.Activity
import com.ds.cardscanner.models.CardDetails
import com.ds.cardscanner.service.DsScannerService

object DsScannerFactory {
    fun create(activity: Activity): DsScannerService {
        return DsScannerService(activity)
    }

    interface OnCardScanResponseListener {
        fun cardScanResponse(cardDetails: CardDetails)
    }
}