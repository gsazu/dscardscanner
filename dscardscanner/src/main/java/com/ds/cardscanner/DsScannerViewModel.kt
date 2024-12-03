package com.ds.cardscanner

import com.ds.cardscanner.init.DsScannerFactory
import com.ds.cardscanner.models.CardDetails

class DsScannerViewModel {
    private var onCardScanResponseListener: DsScannerFactory.OnCardScanResponseListener? = null

    companion object {
        var mInstancae: DsScannerViewModel? = null
    }

    // function for get instance of class
    fun getInstance(): DsScannerViewModel {
        if (mInstancae == null) {
            mInstancae = DsScannerViewModel()
        }
        return mInstancae!!
    }

    fun setCardResponseListener(listener: DsScannerFactory.OnCardScanResponseListener) {
        this.onCardScanResponseListener = listener
    }

    fun setCardResponse(cardDetails: CardDetails) {
        if (this.onCardScanResponseListener != null) {
            this.onCardScanResponseListener!!.cardScanResponse(cardDetails)
        }
    }
}