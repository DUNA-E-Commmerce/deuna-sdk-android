package com.deuna.maven.payment_widget

import android.service.quicksettings.Tile

/**
 * Custom styles that can be passed to the payment widget
 * using the setCustomStyles function.
 */
class PaymentWidgetCustomStyles {

    var hidePoweredBy: Boolean? = null
    var saveButton: SaveButton? = null
    var upperTag: Tag? = null
    var lowerTag: Tag? = null

    /**
     * @param content custom button text
     * @param style custom button colors
     */
    data class SaveButton(val content: String, val style: SaveButtonStyle?) {
        /**
         * @param color hex button text color
         * @param backgroundColor hex background button color
         */
        data class SaveButtonStyle(val color: String, val backgroundColor: String)
    }

    class Tag() {
        var title: Tile? = null

        data class Title(val content: String?)
        data class Description(val content: List<String>, val compact: Boolean?)
    }
}