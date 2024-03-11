package com.deuna.compose_demo.screens

import android.annotation.*
import android.net.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.deuna.compose_demo.*
import com.deuna.compose_demo.view_models.*
import com.deuna.maven.*
import com.deuna.maven.shared.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {
  val navController = LocalNavController.current

  Scaffold {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      val userTokenState = homeViewModel.userToken
      val orderTokenState = homeViewModel.orderToken

      val context = LocalContext.current

      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = orderTokenState.value,
        onValueChange = { orderTokenState.value = it },
        label = { Text("Order Token") }
      )
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = userTokenState.value,
        onValueChange = { userTokenState.value = it },
        label = { Text("User Token") }
      )

      Box(modifier = Modifier.height(20.dp))

      ElevatedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
          homeViewModel.payment(
            context = context,
            completion = { _, error ->
              if (error != null) {
                return@payment
              }
              navController.navigate(
                "/success/${Uri.encode("Payment successful!")}"
              )
            }
          )
        },
      ) {
        Text(text = "Start Payment")
      }

      ElevatedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
          homeViewModel.saveCard(
            context = context,
            completion = { _, error ->
              if (error != null) {
                return@saveCard
              }
              navController.navigate(
                "/success/${Uri.encode("Save Card successful!")}"
              )
            }
          )
        },
      ) {
        Text(text = "Save Card")
      }
    }
  }

}

@Preview
@Composable
private fun Preview() {
  Navigator {
    HomeScreen(
      homeViewModel = HomeViewModel(
        deunaSDK = DeunaSDK(
          environment = Environment.SANDBOX,
          publicApiKey = "FAKE_API_KEY"
        )
      )
    )
  }
}