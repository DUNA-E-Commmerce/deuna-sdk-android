package com.deuna.maven.web_views.deuna.extensions

import com.deuna.maven.DeunaSDK
import com.deuna.maven.initPaymentWidget
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.Json
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.widgets.payment_widget.PaymentWidgetCallbacks

fun DeunaWidget.executeSubmit(callback: (SubmitResult) -> Unit) {
    controller?.executeRemoteFunction(
        jsBuilder = { requestId ->
            return@executeRemoteFunction """
                (function() {
                    ${buildResultFunction(requestId = requestId, type = "submit")}
                    if(typeof window.submit !== 'function'){
                        sendResult({status:"error", message:"Error al procesar la solicitud." });
                        return;
                    }
                    window.submit()
                    .then(sendResult)
                    .catch(error => sendResult({status:"error", message: error.message ?? "Error al procesar la solicitud." }));
                })();
            """.trimIndent()
        },
        callback = { json ->
            callback(
                SubmitResult(
                    status = json["status"] as? String ?: "error",
                    message = json["message"] as? String
                )
            )
        }
    )
}


@Suppress("UNCHECKED_CAST")
fun DeunaWidget.submitStrategy(callback: (SubmitResult) -> Unit) {
    getWidgetState { state ->
        val paymentMethods = state?.get("paymentMethods") as? Json
        val selectedPaymentMethod = paymentMethods?.get("selectedPaymentMethod") as? Json

        if (selectedPaymentMethod == null) {
            executeSubmit(callback)
            return@getWidgetState
        }


        val processorName = selectedPaymentMethod["processor_name"] as? String
        val configuration = selectedPaymentMethod["configuration"] as? Json
        val configFlowType = (configuration?.get("flowType") as? Json)?.get("type") as? String

        val behaviorPaymentMethods = widgetConfig?.behavior?.get("paymentMethods") as? Json
        val behaviorFlowType =
            (behaviorPaymentMethods?.get("flowType") as? Json)?.get("type") as? String

        val isTwoStepFlow =
            (configFlowType == TWO_STEP_FLOW) || (configFlowType == null && behaviorFlowType == TWO_STEP_FLOW)

        if (isTwoStepFlow && processorName == "paypal_wallet") {
            handlePayPalTwoStepFlow(callback)
            return@getWidgetState
        }
        executeSubmit(callback)
    }
}


private fun DeunaWidget.handlePayPalTwoStepFlow(
    callback: (SubmitResult) -> Unit
) {
    DeunaSDK(
        publicApiKey = widgetConfig?.publicApiKey ?: "",
        environment = widgetConfig?.environment ?: Environment.PRODUCTION
    ).apply {
        initPaymentWidget(
            context = context,
            orderToken = widgetConfig?.orderToken ?: "",
            userToken = widgetConfig?.userToken,
            paymentMethods = listOf(
                mapOf(
                    "paymentMethod" to "wallet",
                    "processors" to listOf("paypal_wallet"),
                    "configuration" to mapOf(
                        "express" to true,
                        "flowType" to mapOf("type" to TWO_STEP_FLOW)
                    )
                )
            ),
            callbacks = PaymentWidgetCallbacks().apply {
                onSuccess = { close() }
                onError = { close() }
            }
        )
    }
    callback(SubmitResult(status = "success", message = null))
}