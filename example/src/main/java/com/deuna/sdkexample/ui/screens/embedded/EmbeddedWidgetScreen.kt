package com.deuna.sdkexample.ui.screens.embedded

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.deuna.maven.DeunaSDK
import com.deuna.maven.buildPaymentWidgetUrl
import com.deuna.maven.checkout.domain.CheckoutBridge
import com.deuna.maven.element.domain.ElementsBridge
import com.deuna.maven.payment_widget.domain.PaymentWidgetBridge
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.shared.CheckoutCallbacks
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.Json
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.isValid
import com.deuna.maven.web_views.deuna.extensions.submit
import com.deuna.sdkexample.shared.views.Separator
import com.deuna.sdkexample.ui.screens.embedded.views.PayButton
import com.deuna.sdkexample.ui.screens.main.WidgetToShow
import com.deuna.sdkexample.ui.screens.main.view_model.DEBUG_TAG

data class EmbeddedWidgetScreenParams(
    val deunaSDK: DeunaSDK,
    val orderToken: String,
    val userToken: String,
    val widgetToShow: WidgetToShow
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EmbeddedWidgetScreen(
    params: EmbeddedWidgetScreenParams,
    onSuccess: (data: Json) -> Unit
) {
    val deunaSDK = params.deunaSDK
    val orderToken = params.orderToken
    val userToken = params.userToken
    val widgetToShow = params.widgetToShow

    val deunaWidget = remember { mutableStateOf<DeunaWidget?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8F7))
            .padding(16.dp)
    ) {
        Text("Confirm and pay", style = MaterialTheme.typography.titleLarge)
        Separator(16.dp)

        Card(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    DeunaWidget(context).apply {

                        this.hidePayButton = true
                        var link = ""

                        when (widgetToShow) {
                            WidgetToShow.PAYMENT_WIDGET -> {
                                link = deunaSDK.buildPaymentWidgetUrl(
                                    orderToken = orderToken,
                                    userToken = userToken
                                )
                                bridge = PaymentWidgetBridge(
                                    callbacks = PaymentWidgetCallbacks().apply {
                                        onEventDispatch = { event, data ->
                                            Log.i(DEBUG_TAG, "Event: $event, Data: $data")
                                        }
                                        this.onSuccess = { data ->
                                            Log.i(DEBUG_TAG, "✅ Success: $data")
                                            onSuccess(data)
                                        }
                                    },
                                    deunaWidget = this,
                                )
                            }

                            WidgetToShow.VAULT_WIDGET -> {
                                bridge = ElementsBridge(
                                    callbacks = ElementsCallbacks().apply {
                                        this.onSuccess = { data ->
                                            val savedCard =
                                                (data["metadata"] as Json)["createdCard"] as Json
                                            onSuccess(savedCard)
                                        }
                                    },
                                    deunaWidget = this,
                                )
                            }

                            WidgetToShow.CHECKOUT_WIDGET -> {
                                bridge = CheckoutBridge(
                                    callbacks = CheckoutCallbacks().apply {
                                        this.onSuccess = { data ->
                                            onSuccess(data)
                                        }
                                    },
                                    deunaWidget = this,
                                )
                            }

                            WidgetToShow.CLICK_TO_PAY_WIDGET -> {
                                bridge = ElementsBridge(
                                    callbacks = ElementsCallbacks().apply {
                                        this.onSuccess = { data ->
                                            val savedCard =
                                                (data["metadata"] as Json)["createdCard"] as Json
                                            onSuccess(savedCard)
                                        }
                                    },
                                    deunaWidget = this,
                                )
                            }
                        }

                        // Load the URL inside the DeunaWidget
                        link.isNotEmpty().let {
                            loadUrl(link)
                        }

                        deunaWidget.value = this
                    }
                }
            )
        }


        Separator(16.dp)

        PayButton {
            deunaWidget.value?.let { deunaWidget ->
                deunaWidget.submit { result ->
                    Log.i(DEBUG_TAG, "Submit result: ${result.status} - ${result.message}")
                }
            }
        }

    }

    // Dispose the DeunaWidget when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            deunaWidget.value?.destroy()
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    EmbeddedWidgetScreen(
        params = EmbeddedWidgetScreenParams(
            deunaSDK = DeunaSDK(
                environment = Environment.SANDBOX,
                publicApiKey = "FAKE_API_KEY"
            ),
            orderToken = "FAKE_ORDER_TOKEN",
            userToken = "FAKE_USER_TOKEN",
            widgetToShow = WidgetToShow.PAYMENT_WIDGET
        ),
        onSuccess = {}
    )
}