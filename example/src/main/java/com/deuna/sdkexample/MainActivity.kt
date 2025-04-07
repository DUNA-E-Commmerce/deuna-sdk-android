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
            environment = Environment.DEVELOPMENT,
            publicApiKey = "8049df334279f611e59434fe92958f0238b8c90c12010a0060ae7d9e4f53596fcb917458071ddd5d6a46b00d40a8be4971f23a902892abad2f04e7bbe83e"
        )

        setContent {
            AppNavigation(
                deunaSDK = deunaSDK
            )
        }
    }
}