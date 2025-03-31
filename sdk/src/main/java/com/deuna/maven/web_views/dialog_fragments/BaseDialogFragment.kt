package com.deuna.maven.web_views.dialog_fragments

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.deuna.maven.web_views.base.BaseWebView

abstract class BaseDialogFragment : DialogFragment() {

    lateinit var baseWebView: BaseWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar)
    }

    override fun onDestroyView() {
        baseWebView.destroy()
        super.onDestroyView()
    }
}