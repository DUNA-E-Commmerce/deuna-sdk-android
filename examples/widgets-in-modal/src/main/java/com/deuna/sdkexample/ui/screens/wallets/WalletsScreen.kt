package com.deuna.sdkexample.ui.screens.wallets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.deuna.maven.DeunaSDK
import com.deuna.maven.wallets.WalletProvider
import com.deuna.maven.wallets.WalletsError
import com.deuna.maven.wallets.getWalletsAvailable

@Composable
fun WalletsScreen(
    deunaSDK: DeunaSDK,
    navController: NavController,
) {
    val context = LocalContext.current
    var wallets by remember { mutableStateOf<List<WalletProvider>>(emptyList()) }
    var error by remember { mutableStateOf<WalletsError?>(null) }
    var loading by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Google Pay Wallets",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    loading = true
                    error = null
                    wallets = emptyList()
                    deunaSDK.getWalletsAvailable(context) { result, err ->
                        wallets = result
                        error = err
                        loading = false
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (loading) "Loading..." else "Get Available Wallets")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDED)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Error: ${error!!.code}",
                            color = Color(0xFFB00020),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = error!!.message,
                            color = Color(0xFFB00020),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            } else if (wallets.isEmpty() && !loading) {
                Text(
                    text = "No wallets available",
                    color = Color.Gray,
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(wallets) { wallet ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Text(
                                text = wallet.name,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            ElevatedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Go back")
            }
        }
    }
}
