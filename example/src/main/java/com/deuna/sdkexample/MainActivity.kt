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
            publicApiKey = "2d90d654856056273d469702f25dbd7b686f7f95455fcd692a38bf41c1f361e570dcc006416865b83c7b3eec57429e57dc2c0e4edb2222ccf05ba6f44a68"
        )

        setContent {
            AppNavigation(
                deunaSDK = deunaSDK
            )
        }
    }
}