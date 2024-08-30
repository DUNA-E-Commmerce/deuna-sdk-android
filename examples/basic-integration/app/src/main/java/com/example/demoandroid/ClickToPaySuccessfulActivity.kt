package com.example.demoandroid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClickToPaySuccessfulActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLICK_TO_PAY_DATA = "EXTRA_CLICK_TO_PAY_DATA"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thank_you)

//        val card = JSONObject(intent.getStringExtra(EXTRA_CLICK_TO_PAY_DATA)!!).toMap()
        findViewById<TextView>(R.id.save_card_message).text = """
            Pago con Click To Pay
            Exitoso
        """.trimIndent()
    }

    fun backToMainActivity(view: View?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the current actvity
    }
}