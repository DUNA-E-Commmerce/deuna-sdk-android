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
            publicApiKey = "8ba6d13e322e2c3d3763e4833616b0fa77522b366945c12f1083ebe52dbb03645e8fe7996d6550e6b1d298fd1ce8a6ee3ce4e67eaf50ad61d48211f14724"
        )

        setContent {
            AppNavigation(
                deunaSDK = deunaSDK
            )
        }
    }
}