package com.deuna.maven.payment_widget

import CheckoutResponse
import com.deuna.maven.shared.*
import org.json.JSONObject

typealias OnReFetchOrder = (completion: (CheckoutResponse.Data.Order?) -> Unit) -> Unit
typealias OnSuccess = (data: CheckoutResponse.Data) -> Unit
typealias OnError = (type: PaymentWidgetErrorType) -> Unit
typealias OnCardBinDetected = (PaymentWidgetCallbacks.CardBinMetadata?, OnReFetchOrder) -> Unit
typealias OnInstallmentSelected = (PaymentWidgetCallbacks.InstallmentMetadata?, OnReFetchOrder) -> Unit

// Class defining the different callbacks that can be invoked by the payment widget
class PaymentWidgetCallbacks {
    var onSuccess: OnSuccess? = null
    var onError: OnError? = null
    var onClosed: VoidCallback? = null
    var onCardBinDetected: OnCardBinDetected? = null
    var onInstallmentSelected: OnInstallmentSelected? = null
    var onCanceled: VoidCallback? = null

    data class CardBinMetadata(
        val cardBin: String,
        val cardBrand: String?,
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

    data class InstallmentMetadata(
        val cardBin: String,
        val planOptionId: String,
        val displayInstallmentLabel: String,
        val displayInstallmentsAmount: String,
        val installments: Int,
        val installmentRate: Int,
    ) {
        companion object {
            fun fromJson(metadata: JSONObject): InstallmentMetadata {
                return InstallmentMetadata(
                    cardBin = metadata.getString("card_bin"),
                    planOptionId = metadata.getString("plan_option_id"),
                    displayInstallmentLabel = metadata.getString("display_installment_label"),
                    displayInstallmentsAmount = metadata.getString("display_installments_amount"),
                    installments = metadata.optInt("installments"),
                    installmentRate = metadata.getInt("installment_rate")
                )
            }
        }
    }
}


