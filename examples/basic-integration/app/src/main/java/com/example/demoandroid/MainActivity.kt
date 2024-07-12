package com.example.demoandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.DeunaSDK
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.closeCheckout
import com.deuna.maven.closeElements
import com.deuna.maven.closePaymentWidget
import com.deuna.maven.initCheckout
import com.deuna.maven.initElements
import com.deuna.maven.initPaymentWidget
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.setCustomCss
import com.deuna.maven.shared.*
import org.json.JSONObject

val ERROR_TAG = "❌ DeunaSDK"
val DEBUG_TAG = "👀 DeunaSDK"

class MainActivity : AppCompatActivity() {
    private val deunaSdk = DeunaSDK(
        environment = Environment.SANDBOX,
        publicApiKey = "85d9c1d546e33d01fa92f4a4ead4bb4dc3c95ed4c61fedfc771c7a599acc605d6a385174b200ec25dc9a7f7ee74f11738fa62d4184ab09c0ebe40094ea32",
    );

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val payButton: Button = findViewById(R.id.payButton)
        val paymentWidgetButton: Button = findViewById(R.id.paymentWidgetButton)
        val savePaymentMethodButton: Button = findViewById(R.id.savePaymentMethodButton)


        findViewById<EditText>(R.id.inputOrderToken).editableText.append("bb3d2aad-7ffd-4089-9a2c-ab640c2817c2")

        payButton.setOnClickListener { startPaymentProcess() }
        paymentWidgetButton.setOnClickListener { showPaymentWidget() }
        savePaymentMethodButton.setOnClickListener { saveCard() }
    }


    private fun showPaymentWidget() {
        val orderToken: String = findViewById<EditText>(R.id.inputOrderToken).text.toString().trim()

        deunaSdk.initPaymentWidget(
            context = this,
            orderToken = orderToken,
            callbacks = PaymentWidgetCallbacks().apply {
                onSuccess = { data ->
                    deunaSdk.closePaymentWidget()
                    Intent(this@MainActivity, PaymentSuccessfulActivity::class.java).apply {
                        putExtra(
                            PaymentSuccessfulActivity.EXTRA_JSON_ORDER,
                            JSONObject(data["order"] as Json).toString(),
                        )
                        startActivity(this)
                    }
                }

                onCanceled = {
                    Log.d(DEBUG_TAG, "Payment was canceled by user")
                }

                onCardBinDetected = { cardBinMetadata, refetchOrder ->
                    Log.d(DEBUG_TAG, "cardBinMetadata: $cardBinMetadata")

                    if (cardBinMetadata != null) {
                        val customStyles = mapOf(
                            "upperTag" to mapOf(
                                "description" to mapOf(
                                    "content" to listOf("text 1", "text 2"),
                                    "compact" to true,
                                    "listDivider" to "line"
                                )
                            )
                        )

                        /*
                        customStyles is equivalent to the next JSON
                        {
                            upperTag: {
                                description: {
                                   content: ["text 1", "text 2"],
                                   compact: true,
                                   listDivider: "line",
                                 },
                            },
                          }
                         */
                        deunaSdk.setCustomCss(
                            data = customStyles
                        )

                        refetchOrder { order ->
                            Log.d(DEBUG_TAG, "onRefetchOrder: $order")
                        }

                    }

                }
                onClosed = {
                    Log.d(DEBUG_TAG, "DEUNA widget was closed")
                }
            })
    }


    private fun startPaymentProcess() {
        val orderToken: String = findViewById<EditText>(R.id.inputOrderToken).text.toString().trim()

        deunaSdk.initCheckout(
            context = this,
            orderToken = orderToken,
            callbacks = CheckoutCallbacks().apply {
                onSuccess = { data ->
                    Log.d(DEBUG_TAG, "Payment success $data")
                    deunaSdk.closeCheckout()
                    Intent(this@MainActivity, PaymentSuccessfulActivity::class.java).apply {
                        putExtra(
                            PaymentSuccessfulActivity.EXTRA_JSON_ORDER,
                            JSONObject(data["order"] as Json).toString(),
                        )
                        startActivity(this)
                    }
                }
                onError = {
                    Log.e(ERROR_TAG, it.type.message)
                    deunaSdk.closeCheckout()
                }
                onCanceled = {
                    Log.d(DEBUG_TAG, "Payment was canceled by user")
                }
                eventListener = { type, _ ->
                    Log.d("✅ ON EVENT", type.name)
                    when (type) {
                        CheckoutEvent.changeAddress, CheckoutEvent.changeCart -> {
                            deunaSdk.closeCheckout()
                        }

                        else -> {}
                    }
                }
                onClosed = {
                    Log.d(DEBUG_TAG, "DEUNA widget was closed")
                }
            })
    }


    private fun saveCard() {
        val userToken: String = findViewById<EditText>(R.id.inputUserToken).text.toString().trim()

        deunaSdk.initElements(
            context = this,
            userToken = userToken,
            callbacks = ElementsCallbacks().apply {
                deunaSdk.closeElements()
                onSuccess = {
                    Intent(this@MainActivity, ThankYouActivity::class.java).apply {
                        startActivity(this)
                    }
                }
                eventListener = { type, _ ->
                    Log.d(DEBUG_TAG, "eventListener ${type.name}")
                }
                onError = {
                    Log.e(ERROR_TAG, it.type.message)
                    deunaSdk.closeElements()
                }
                onCanceled = {
                    Log.d(DEBUG_TAG, "Saving card was canceled by user")
                }
                onClosed = {
                    Log.d(DEBUG_TAG, "DEUNA widget was closed")
                }
            })
    }
}