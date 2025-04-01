package com.deuna.maven.web_views.dialog_fragments.base

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.deuna.maven.web_views.base.BaseWebView

abstract class BaseDialogFragment : DialogFragment() {

    lateinit var baseWebView: BaseWebView

    abstract fun onBackButtonPressed()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity()) {
            override fun onBackPressed() {
                onBackButtonPressed()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        baseWebView.destroy()
        super.onDestroyView()
    }
}