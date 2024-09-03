package com.example.demoandroid

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.*
import com.example.demoandroid.screens.PaymentSuccessfulActivity
import org.json.JSONObject


const val ERROR_TAG = "❌ DeunaSDK"
const val DEBUG_TAG = "👀 DeunaSDK"

@Suppress("UNCHECKED_CAST")
class MainActivity : AppCompatActivity() {
    val deunaSdk = DeunaSDK(
        environment = Environment.SANDBOX,
        publicApiKey = "abca06bc456b1459fa843aa8c6bfe1598c8f520f34de9a1484ead3454feb8c2a6855de28a45e5b96e7e005bfde619a65f03db41394e8b94c48e6fb239d6d",
    )

    val context: Context
        get() = this@MainActivity

    val orderToken: String
        get() = findViewById<EditText>(R.id.inputOrderToken).text.toString().trim()

    val userToken: String?
        get() {
            val text = findViewById<EditText>(R.id.inputUserToken).text.toString().trim()
            return text.ifEmpty { null }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.payButton).setOnClickListener {
            startPaymentProcess()
        }

        findViewById<Button>(R.id.paymentWidgetButton).setOnClickListener {
            showPaymentWidget()
        }

        findViewById<Button>(R.id.savePaymentMethodButton).setOnClickListener {
            initElementsVaultWidget()
        }

        findViewById<Button>(R.id.clickToPayButton).setOnClickListener {
            clickToPay()
        }
    }

    fun handlePaymentSuccess(data: Json) {
        Intent(this@MainActivity, PaymentSuccessfulActivity::class.java).apply {
            putExtra(
                PaymentSuccessfulActivity.EXTRA_JSON_ORDER,
                JSONObject(data["order"] as Json).toString()
            )
            startActivity(this)
        }
    }

    fun showPaymentErrorAlertDialog(metadata: PaymentsError.Metadata) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(metadata.code)
        builder.setMessage(metadata.message)

        builder.setPositiveButton(
            "Aceptar",
        ) { dialog, _ -> // Code to execute when OK button is clicked
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}