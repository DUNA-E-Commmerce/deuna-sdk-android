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
                        environment = Environment.STAGING,
                        publicApiKey = "2d90d654856056273d469702f25dbd7b686f7f95455fcd692a38bf41c1f361e570dcc006416865b83c7b3eec57429e57dc2c0e4edb2222ccf05ba6f44a68"
                    )
                )
            )
        }
        composable("compose-example") {

        }
    }
}