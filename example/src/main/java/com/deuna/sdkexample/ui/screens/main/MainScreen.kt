package com.deuna.sdkexample.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.Environment
import com.deuna.sdkexample.shared.views.Separator
import com.deuna.sdkexample.ui.screens.main.utils.showWidgetInModal
import com.deuna.sdkexample.ui.screens.main.view_model.MainViewModel
import com.deuna.sdkexample.ui.screens.main.views.Inputs
import com.deuna.sdkexample.ui.screens.main.views.ViewModePicker


enum class ViewMode(val label: String) {
    MODAL("Modal"),
    EMBEDDED("Embedded");
}

enum class WidgetToShow(val label: String) {
    PAYMENT_WIDGET("Payment Widget"),
    CHECKOUT_WIDGET("Checkout Widget"),
    VAULT_WIDGET("Vault Widget"),
    CLICK_TO_PAY_WIDGET("Click to Pay Widget"),
    ;
}

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    // Retrieve the user token and order token states from the view model
    val userTokenState = viewModel.userToken
    val orderTokenState = viewModel.orderToken

    // Retrieve the Context from the composition's LocalContext
    val context = LocalContext.current

    var selectedViewMode by remember { mutableStateOf(ViewMode.MODAL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Inputs(
            orderTokenState = orderTokenState,
            userTokenState = userTokenState
        )

        Separator(20.dp)

        ViewModePicker(
            selectedViewMode = selectedViewMode
        ) { selectedViewMode = it }

        Separator(30.dp)

        WidgetToShow.entries.forEach { widget ->
            Button(
                onClick = {
                    when (selectedViewMode) {
                        ViewMode.MODAL -> {
                            showWidgetInModal(
                                context = context,
                                viewModel = viewModel,
                                widgetToShow = widget
                            )
                        }

                        ViewMode.EMBEDDED -> {}
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                )
            ) {
                Text(widget.label)
            }
        }

    }
}

@Preview(showBackground = true, name = "MyScreen Preview")
@Composable
fun MyScreenPreview() {
    MainScreen(
        viewModel = MainViewModel(
            deunaSDK = DeunaSDK(
                environment = Environment.SANDBOX,
                publicApiKey = "FAKE_API_KEY"
            )
        )
    )
}