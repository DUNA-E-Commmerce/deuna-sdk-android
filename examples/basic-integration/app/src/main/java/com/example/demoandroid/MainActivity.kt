package com.example.demoandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
//import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.deuna.maven.DeunaSDK
import com.deuna.maven.checkout.CheckoutCallbacks
import com.deuna.maven.checkout.CheckoutEvent
import com.deuna.maven.checkout.domain.ElementType
import com.deuna.maven.closeCheckout
import com.deuna.maven.closeElements
import com.deuna.maven.element.domain.ElementsCallbacks
import com.deuna.maven.element.domain.ElementsEvent
import com.deuna.maven.initCheckout
import com.deuna.maven.initElements
import com.deuna.maven.shared.Environment


val ERROR_TAG = "❌ DeunaSDK"
val DEBUG_TAG = "👀 DeunaSDK"

class MainActivity : AppCompatActivity() {
  private var apiKey = ""
  private var environment: Environment = Environment.STAGING

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val payButton: Button = findViewById(R.id.payButton)
    val savePaymentMethodButton: Button = findViewById(R.id.savePaymentMethodButton)

    payButton.setOnClickListener { initCheckout() }
    savePaymentMethodButton.setOnClickListener { initElements() }
  }

  private fun applyConfig() {
    val inputApiKeyEditText: EditText = findViewById(R.id.inputApiKey)
    val environmentSpinner: Spinner = findViewById(R.id.environmentOption)

    apiKey = inputApiKeyEditText.text.toString().trim()

    environment = when (environmentSpinner.selectedItemPosition) {
      0 -> Environment.STAGING
      1 -> Environment.PRODUCTION
      2 -> Environment.DEVELOPMENT
      else -> Environment.SANDBOX
    }
  }

  private fun configureForCheckout() {
    DeunaSDK.initialize(
      environment = environment,
      privateApiKey = apiKey,
      publicApiKey = "YOUR_PUBLIC_API_KEY"
    )
  }

  private fun configureForElements() {
    DeunaSDK.initialize(
      environment = environment,
      privateApiKey = "YOUR_PRIVATE_API_KEY",
      publicApiKey = apiKey
    )
  }

  private fun initCheckout() {
    applyConfig()
    configureForCheckout()
    val orderToken: String = findViewById<EditText>(R.id.inputOrderToken).text.toString().trim()

    val callbacks = CheckoutCallbacks().apply {
      onSuccess = {
        Intent(this@MainActivity, ThankYouActivity::class.java).apply {
          startActivity(this)
        }
      }
      onError = {
        Log.e(ERROR_TAG, it?.type?.message ?: "Unknown error")
        closeCheckout()
      }
      eventListener = { type, _ ->
        Log.d(DEBUG_TAG, "eventListener ${type.name}")
      }
      onClose = {
        Log.d(DEBUG_TAG, "Checkout was closed")
      }
    }

    DeunaSDK.shared.initCheckout(
      context = this,
      orderToken = orderToken,
      callbacks = callbacks,
      closeEvents = setOf(
        CheckoutEvent.apmSuccess,
        CheckoutEvent.purchase,
        CheckoutEvent.linkFailed,
        CheckoutEvent.changeCart,
        CheckoutEvent.changeAddress
      )
    )
  }


  private fun initElements() {
    applyConfig()
    configureForElements()
    val userToken: String = findViewById<EditText>(R.id.inputUserToken).text.toString().trim()

    val callbacks = ElementsCallbacks().apply {
      onSuccess = {
        Intent(this@MainActivity, ThankYouActivity::class.java).apply {
          startActivity(this)
        }
      }
      eventListener = { type, _ ->
        Log.d(DEBUG_TAG, "eventListener ${type.name}")
      }
      onError = {
        Log.e(ERROR_TAG, it?.type?.message ?: "Unknown error")
        closeElements()
      }
      onClose = {
        Log.d(DEBUG_TAG, "Checkout was closed")
      }
    }

    DeunaSDK.shared.initElements(
      context = this,
      userToken = userToken,
      callbacks = callbacks,
      closeEvents = setOf(ElementsEvent.vaultSaveSuccess, ElementsEvent.cardSuccessfullyCreated)
    )
  }

  private fun closeCheckout() {
    DeunaSDK.shared.closeCheckout(this)
  }

  private fun closeElements() {
    DeunaSDK.shared.closeElements(this)
  }
}