package com.deuna.compose_demo

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.deuna.compose_demo.screens.*
import com.deuna.compose_demo.view_models.*
import com.deuna.maven.*
import com.deuna.maven.shared.Environment

class MainActivity() : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      Navigator {
        NavHost(
          navController = LocalNavController.current,
          startDestination = Screens.Home.route // home screen as a default route
        ) {
          composable(Screens.Home.route) {
            HomeScreen(
              homeViewModel = HomeViewModel(
                deunaSDK = DeunaSDK(
                  environment = Environment.STAGING,
                  publicApiKey = "e40affdfbee57e43de41d1ce1451859bbe85626c1e87adaa93e538a6fb68488d09bb578f561122c1177e66ab1238563359acb70aa0b972ac8f44a52bceb7"
                ),
              )
            )
          }
          composable(Screens.Success.route) {
            SuccessScreen(message = it.arguments?.getString("message") ?: "Thank You")
          }
        }
      }
    }
  }
}

