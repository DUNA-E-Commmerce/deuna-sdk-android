package com.deuna.maven

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import mx.com.bancoazteca.azteca360.sdk.BAFPConfigSDK
import mx.com.bancoazteca.azteca360.sdk.providers.BAFPCreditProvider
import mx.com.bancoazteca.azteca360.sdk.utils.enums.BAFPApp
import mx.com.bancoazteca.azteca360.sdk.utils.enums.BAFPCardStatus
import mx.com.bancoazteca.azteca360.sdk.utils.enums.BAFPEnvironment


class BazActivity : AppCompatActivity() {

    var provider: BAFPCreditProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BAFPConfigSDK.setSDK(
            environment = BAFPEnvironment.QA,
            activity = this,
            channel = BAFPApp.Elektra,
            user = "leonardosalazar@gmail.com"
        )

        setContentView(R.layout.baz_activity)
        provider = BAFPCreditProvider(
            this,
            BAFPCardStatus.OTP_PHONE,
            findViewById(R.id.bafp_credit_login),
            findViewById(R.id.bafp_credit_otp)
        )

        provider!!.setOnGenericCallBacks(
            onError = {
                Toast.makeText(
                    this, "${it.message} - ${it.details}", Toast.LENGTH_SHORT
                ).show()
            },
            onTagLogger = {
                Log.d("👀 BAZ SDK", "${it.id} - ${it.message}")
            },
        )

        provider!!.initCreditLoginOTP(
            token = "pU4KE5ECjqwKuT6nUKacW5GtdDvf",
            idPayment = "5DBFFC6E-9092-7542-F790-F3D13E9764C5",
            type = { type ->
                Log.d("👀 BAZ SDK", "initCreditLoginOTP > type $type")

            },
            successPhone = { isSuccess, resultException ->
                Log.d(
                    "👀 BAZ SDK",
                    " initCreditLoginOTP > successPhone > $isSuccess - ${resultException?.message}"
                )
            },
            resultOTP = { isSuccess, resultException ->
                Log.d(
                    "👀 BAZ SDK",
                    "initCreditLoginOTP > resultOTP > $isSuccess - ${resultException?.message}"
                )
            },
            isSecure = {
                Log.d("👀 BAZ SDK", " initCreditLoginOTP > isSecure > $it")
            },
        )
    }
}
