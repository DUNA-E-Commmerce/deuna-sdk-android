package com.deuna.explore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.deuna.maven.web_views.ExternalUrlHelper
import com.deuna.explore.presentation.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ExternalUrlHelper.registerForActivityResult(this)

        setContent {
            AppNavigation()
        }
    }
}
