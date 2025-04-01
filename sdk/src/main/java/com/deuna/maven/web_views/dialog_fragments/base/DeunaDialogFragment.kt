package com.deuna.maven.web_views.dialog_fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.R
import com.deuna.maven.web_views.deuna.DeunaWebView

abstract class DeunaDialogFragment : BaseDialogFragment(){

    val deunaWebView: DeunaWebView get() = baseWebView as DeunaWebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.deuna_webview_container, container, false)
        baseWebView = view.findViewById(R.id.deuna_webview)
        return view
    }

    override fun onBackButtonPressed() {
        if (!deunaWebView.closeEnabled) {
            return
        }
        deunaWebView.bridge?.onCloseByUser?.let { it() }
    }
}