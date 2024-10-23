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

class MainActivity : AppCompatActivity() {
    val deunaSdk = DeunaSDK(
        environment = Environment.STAGING,
        publicApiKey = "bc26fddbaf71313136318bd9ba20a965b05f2aecdd0dd9969730157e90010ddccc1baa251baf2dd5aa14822c24ba2b42f5ea2509b488cfb0c9e047ad7a9a",
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

    fun handlePaymentSuccess(order: Json) {
        Intent(this@MainActivity, PaymentSuccessfulActivity::class.java).apply {
            putExtra(
                PaymentSuccessfulActivity.EXTRA_JSON_ORDER,
                JSONObject(order).toString()
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