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
import com.deuna.maven.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.shared.*


val ERROR_TAG = "❌ DeunaSDK"
val DEBUG_TAG = "👀 DeunaSDK"

class MainActivity : AppCompatActivity() {
  private val deunaSdk = DeunaSDK(
    environment = Environment.STAGING,
    publicApiKey = "7be2cfe6cbd7901a48c144296a5d939095322bd5ed4ccad3c8c16850e071c0b39ef18ffb9063bb76bd96262bcd3cc88d936b299f0c9b8ce902141a0afe44",
  );

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val payButton: Button = findViewById(R.id.payButton)
    val paymentWidgetButton: Button = findViewById(R.id.paymentWidgetButton)
    val savePaymentMethodButton: Button = findViewById(R.id.savePaymentMethodButton)

    payButton.setOnClickListener { startPaymentProcess() }
    paymentWidgetButton.setOnClickListener { showPaymentWidget() }
    savePaymentMethodButton.setOnClickListener { saveCard() }
  }


  private fun showPaymentWidget() {
    val orderToken: String = findViewById<EditText>(R.id.inputOrderToken).text.toString().trim()

    deunaSdk.initPaymentWidget(context = this, orderToken = orderToken, callbacks = PaymentWidgetCallbacks().apply {
      onPaymentSuccess = {
        deunaSdk.closePaymentWidget(this@MainActivity)
        Intent(this@MainActivity, ThankYouActivity::class.java).apply {
          startActivity(this)
        }
      }
      onClosed = {
        Log.d(DEBUG_TAG, "DEUNA widget was closed")
      }
    })
  }



  private fun startPaymentProcess() {
    val orderToken: String = findViewById<EditText>(R.id.inputOrderToken).text.toString().trim()

    deunaSdk.initCheckout(context = this, orderToken = orderToken, callbacks = CheckoutCallbacks().apply {
      onSuccess = {
        deunaSdk.closeCheckout(this@MainActivity)
        Intent(this@MainActivity, ThankYouActivity::class.java).apply {
          startActivity(this)
        }
      }
      onError = {
        Log.e(ERROR_TAG, it.type.message)
        deunaSdk.closeCheckout(this@MainActivity)
      }
      onCanceled = {
        Log.d(DEBUG_TAG, "Payment was canceled by user")
      }
      eventListener = { type, _ ->
        Log.d("✅ ON EVENT", type.name)
        when (type) {
          CheckoutEvent.changeAddress, CheckoutEvent.changeCart -> {
            deunaSdk.closeCheckout(this@MainActivity)
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

    deunaSdk.initElements(context = this, userToken = userToken, callbacks = ElementsCallbacks().apply {
      deunaSdk.closeElements(this@MainActivity)
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
        deunaSdk.closeElements(this@MainActivity)
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