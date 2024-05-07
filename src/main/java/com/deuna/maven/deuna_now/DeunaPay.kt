package com.deuna.maven.deuna_now

import android.content.Context
import com.deuna.maven.shared.*

/**
 *  DeunaPay class encapsulating the functionality of the payment widget
 */
class DeunaPay(val orderToken: String, val environment: Environment) {

    var callbacks: DeunaPayCallbacks? = null

    /**
     * Method to show the widget based on provided parameters
     */
    fun show(context: Context, callbacks: DeunaPayCallbacks) {
        this.callbacks = callbacks
    }

    /**
     * Set Custom styles to the payment widget when the
     * onCardBinDetected callback is called
     */
    fun setCustomStyles(customStyles: DeunaPayCustomStyles) {}
}