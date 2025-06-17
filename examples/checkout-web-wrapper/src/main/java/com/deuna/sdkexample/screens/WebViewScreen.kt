package com.example.checkoutwebwrapper.screens

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.deuna.sdkexample.web_view.WebViewWrapper


@Composable
fun WebViewScreen(url: String) {
    val context = LocalContext.current
    val webViewWrapper = remember {
       WebViewWrapper(context)
    }


    DisposableEffect(webViewWrapper) {
        webViewWrapper.loadUrl(url)
        onDispose {
//            controller.destroy()
        }
    }

    AndroidView(
        factory = { webViewWrapper },
        modifier = Modifier.fillMaxSize()
    )
}