package com.deuna.sdkexample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.Environment
import com.deuna.sdkexample.navigation.AppNavigation

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deunaSDK = DeunaSDK(
            environment = Environment.STAGING,
            publicApiKey = "16f23a0f9f0a4ce13f2bd34847143b6b544b352082b4bedad4aabe69e4116c4858c03ccae00a20c52c72366ec5d484ad4420977db0e299c8b894dfee8cab"
        )

        setContent {
            AppNavigation(
                deunaSDK = deunaSDK
            )
        }
    }
}