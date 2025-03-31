package com.deuna.maven.web_views.deuna.extensions

import com.deuna.maven.shared.Json
import com.deuna.maven.web_views.deuna.DeunaWebView


fun DeunaWebView.buildResultFunction(requestId: Int, type: String): String {
    return """
    function sendResult(data){
        window.webkit.messageHandlers.$remoteJsFunctionsBridgeName.postMessage(JSON.stringify({type:"$type$", data: data , requestId: $requestId$) }));
    }     
    """.trimIndent()
}

fun DeunaWebView.refetchOrder(callback: (Json?) -> Unit) {
    executeRemoteFunction(
        jsBuilder = { requestId ->
            return@executeRemoteFunction """
                 (function() {
                    ${buildResultFunction(requestId = requestId, type = "refetchOrder")}
                       if(typeof window.deunaRefetchOrder !== 'function'){
                           sendResult({ order:null });
                           return;
                       }
                          
                       window.deunaRefetchOrder()
                       .then(sendResult)
                       .catch(error => sendResult({ order:null }));
                 })();
            """.trimIndent()
        },
        callback = { json ->
            val order = json["order"] as? Json
            callback(order)
        }
    )
}

fun DeunaWebView.isValid(callback: (Boolean) -> Unit) {
    executeRemoteFunction(
        jsBuilder = { requestId ->
            return@executeRemoteFunction """
                (function() {
                    ${buildResultFunction(requestId = requestId, type = "isValid")}
                    if(typeof window.isValid !== 'function'){
                        sendResult({isValid:false});
                        return;
                    }
                    sendResult( {isValid: window.isValid() });
                })();
            """.trimIndent()
        },
        callback = { json ->
            val isValid = json["isValid"] as? Boolean
            callback(isValid ?: false)
        }
    )
}

fun DeunaWebView.submit(callback: (SubmitResult) -> Unit){
    executeRemoteFunction(
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

fun DeunaWebView.getWidgetState(callback: (Json?) -> Unit){
    executeRemoteFunction(
        jsBuilder = { requestId ->
            return@executeRemoteFunction """
            (function() {
                ${buildResultFunction(requestId = requestId, type = "getWidgetState")}
                if(!window.deunaWidgetState){
                    sendResult({ deunaWidgetState: null });
                    return;
                }
                sendResult({ deunaWidgetState: window.deunaWidgetState });
            })();
            """.trimIndent()
        },
        callback = { json ->
            callback(
                json["deunaWidgetState"] as? Json
            )
        }
    )
}

data class SubmitResult(val status:String, val message: String?)