package com.deuna.maven.payment_widget

import CheckoutResponse
import com.deuna.maven.shared.*

typealias OnReFetchOrder = (completion: (CheckoutResponse.Data.Order?) -> Unit) -> Unit
typealias OnSuccess = (data: CheckoutResponse.Data) -> Unit
typealias OnFailure = (data: CheckoutResponse.Data) -> Unit
typealias OnCardBinDetected = (PaymentWidgetCallbacks.CardBinMetadata, OnReFetchOrder) -> Unit

// Class defining the different callbacks that can be invoked by the payment widget
class PaymentWidgetCallbacks {
    var onPaymentSuccess: OnSuccess? = null
    var onPaymentFailure: OnFailure? = null
    var onClosed: VoidCallback? = null
    var onCardBinDetected: OnCardBinDetected? = null

    data class CardBinMetadata(val cardBin: String, val cardBrand: String)
}


