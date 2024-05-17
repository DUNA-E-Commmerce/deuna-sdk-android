package com.deuna.maven.payment_widget

import CheckoutResponse
import com.deuna.maven.shared.*
import org.json.JSONObject

typealias OnReFetchOrder = (completion: (CheckoutResponse.Data.Order?) -> Unit) -> Unit
typealias OnSuccess = (data: CheckoutResponse.Data) -> Unit
typealias OnError = (type: PaymentWidgetErrorType) -> Unit
typealias OnCardBinDetected = (PaymentWidgetCallbacks.CardBinMetadata?, OnReFetchOrder) -> Unit

// Class defining the different callbacks that can be invoked by the payment widget
class PaymentWidgetCallbacks {
    var onSuccess: OnSuccess? = null
    var onError: OnError? = null
    var onClosed: VoidCallback? = null
    var onCardBinDetected: OnCardBinDetected? = null
    var onCanceled: VoidCallback? = null

    data class CardBinMetadata(
        val cardBin: String,
        val cardBrand: String,
        val installmentPlanOptionId: String?
    ) {

        companion object {
            fun fromJson(metadata: JSONObject): CardBinMetadata {
                val hasInstallment = metadata.has("installmentPlanOptionId")
                return CardBinMetadata(
                    cardBin = metadata.getString("cardBin"),
                    cardBrand = metadata.getString("cardBrand"),
                    installmentPlanOptionId = if (hasInstallment) metadata.getString(
                        "installmentPlanOptionId"
                    ) else null,
                )
            }
        }
    }
}


