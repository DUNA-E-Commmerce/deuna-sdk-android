package com.deuna.sdkexample.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.Environment
import com.deuna.sdkexample.ui.screens.main.MainScreen
import com.deuna.sdkexample.ui.screens.main.view_model.MainViewModel


@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    NavHost(navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                MainViewModel(
                    deunaSDK = DeunaSDK(
                        environment = Environment.SANDBOX,
                        publicApiKey = "YOUR_PUBLIC_API_KEY"
                    )
                )
            )
        }
        composable("compose-example") {

        }
    }
}