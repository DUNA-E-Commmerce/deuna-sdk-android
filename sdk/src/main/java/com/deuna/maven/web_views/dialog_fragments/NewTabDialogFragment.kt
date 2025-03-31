package com.deuna.maven.web_views.dialog_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.R

class NewTabDialogFragment(private val url: String, val onDialogDestroyed: () -> Unit) : BaseDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.deuna_webview_container, container, false)
        baseWebView = view.findViewById(R.id.deuna_webview)
        baseWebView.loadUrl(url)
        return view
    }

    override fun onDestroyView() {
        onDialogDestroyed()
        super.onDestroyView()
    }
}