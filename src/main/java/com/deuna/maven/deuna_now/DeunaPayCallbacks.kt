package com.deuna.maven.deuna_now

import CheckoutResponse
import com.deuna.maven.shared.*

typealias OnReFetchOrder = (completion: (CheckoutResponse.Data.Order?) -> Unit) -> Unit
typealias OnSuccess = (data: CheckoutResponse.Data) -> Unit
typealias OnFailure = (data: CheckoutResponse.Data) -> Unit
typealias OnCardBinDetected = (DeunaPayCallbacks.CardBinMetadata, OnReFetchOrder) -> Unit

// Class defining the different callbacks that can be invoked by the payment widget
class DeunaPayCallbacks {
    var onPaymentSuccess: OnSuccess? = null
    var onPaymentFailure: OnFailure? = null
    var onClosed: VoidCallback? = null
    var onCardBinDetected: OnCardBinDetected? = null

    data class CardBinMetadata(val cardBin: String, val cardBrand: String)
}


