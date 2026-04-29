package com.deuna.maven.wallets

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.shared.Callbacks
import com.deuna.maven.shared.ElementsCallbacks
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import java.util.concurrent.Executors

class WalletPaymentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PAYMENT_REQUEST_JSON = "PAYMENT_REQUEST_JSON"
        const val EXTRA_USER_TOKEN = "USER_TOKEN"
        const val EXTRA_USER_ID = "USER_ID"
        const val EXTRA_ENVIRONMENT = "ENVIRONMENT"
        const val EXTRA_PUBLIC_API_KEY = "PUBLIC_API_KEY"
        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

        @Volatile
        internal var pendingCallbacks: com.deuna.maven.shared.ElementsCallbacks? = null
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val workers = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestJson = intent.getStringExtra(EXTRA_PAYMENT_REQUEST_JSON)
        if (requestJson == null) {
            DeunaLogs.error("[wallets] WalletPaymentActivity: missing payment request")
            finish()
            return
        }

        val envName = intent.getStringExtra(EXTRA_ENVIRONMENT) ?: Environment.PRODUCTION.name
        val environment = Environment.valueOf(envName)

        val googlePayEnv = if (environment == Environment.PRODUCTION)
            WalletConstants.ENVIRONMENT_PRODUCTION
        else
            WalletConstants.ENVIRONMENT_TEST

        val paymentsClient = Wallet.getPaymentsClient(
            this,
            Wallet.WalletOptions.Builder().setEnvironment(googlePayEnv).build()
        )

        val request = PaymentDataRequest.fromJson(requestJson)
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(request),
            this,
            LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != LOAD_PAYMENT_DATA_REQUEST_CODE) return

        val callbacks = pendingCallbacks
        pendingCallbacks = null

        when (resultCode) {
            Activity.RESULT_OK -> {
                val paymentData = PaymentData.getFromIntent(data!!)
                if (paymentData == null) {
                    DeunaLogs.error("[wallets] PaymentData is null on RESULT_OK")
                    dispatchError(callbacks, "PAYMENT_DATA_NULL", "Payment data not received.")
                    finish()
                    return
                }

                val paymentDataJson = paymentData.toJson()
                val userToken = intent.getStringExtra(EXTRA_USER_TOKEN)
                val userId = intent.getStringExtra(EXTRA_USER_ID)
                val publicApiKey = intent.getStringExtra(EXTRA_PUBLIC_API_KEY) ?: ""
                val envName = intent.getStringExtra(EXTRA_ENVIRONMENT) ?: Environment.PRODUCTION.name
                val environment = Environment.valueOf(envName)

                if (!userToken.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                    // Elements SSR path: tokenize via DEUNA API
                    workers.execute {
                        try {
                            val apiResponse = TokenizeWalletCard.tokenize(
                                environment = environment,
                                publicApiKey = publicApiKey,
                                userId = userId,
                                userToken = userToken,
                                paymentDataJson = paymentDataJson,
                            )
                            val hasError = apiResponse.has("error") && !apiResponse.isNull("error")
                            if (hasError) {
                                val err = apiResponse.optJSONObject("error")
                                dispatchError(
                                    callbacks,
                                    err?.optString("code") ?: "TOKENIZATION_ERROR",
                                    err?.optString("message") ?: "Card tokenization returned an error.",
                                )
                            } else {
                                val resultMap = jsonToMap(apiResponse)
                                mainHandler.post { callbacks?.onSuccess?.invoke(resultMap) }
                            }
                        } catch (e: Exception) {
                            DeunaLogs.error("[wallets] Tokenization failed: ${e.message}")
                            dispatchError(callbacks, "TOKENIZATION_REQUEST_FAILED", e.message ?: "Unknown error")
                        } finally {
                            mainHandler.post { finish() }
                        }
                    }
                } else {
                    // Payment Widget path: return raw payment data
                    val resultMap = jsonToMap(org.json.JSONObject(paymentDataJson))
                    mainHandler.post {
                        callbacks?.onSuccess?.invoke(resultMap)
                        finish()
                    }
                }
            }

            Activity.RESULT_CANCELED -> {
                mainHandler.post {
                    callbacks?.onClosed?.invoke(CloseAction.userAction)
                    finish()
                }
            }

            AutoResolveHelper.RESULT_ERROR -> {
                val status = AutoResolveHelper.getStatusFromIntent(data)
                val message = status?.statusMessage ?: "Google Pay error"
                DeunaLogs.error("[wallets] Google Pay error: $message (code=${status?.statusCode})")
                dispatchError(callbacks, "GOOGLE_PAY_ERROR", message)
                finish()
            }

            else -> finish()
        }
    }

    private fun dispatchError(callbacks: com.deuna.maven.shared.ElementsCallbacks?, code: String, message: String) {
        val error = com.deuna.maven.widgets.elements_widget.ElementsError(
            type = com.deuna.maven.widgets.elements_widget.ElementsError.Type.UNKNOWN_ERROR,
            metadata = com.deuna.maven.widgets.elements_widget.ElementsError.Metadata(
                code = code,
                message = message,
            )
        )
        mainHandler.post { callbacks?.onError?.invoke(error) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun jsonToMap(json: org.json.JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = json.get(key)
        }
        return map
    }
}
