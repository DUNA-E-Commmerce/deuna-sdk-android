package com.deuna.maven.element.domain

import android.app.Activity
import org.json.JSONObject

class ElementCallbacks {
    var onSuccess: ((ElementSuccessResponse) -> Unit)? = null
    var onError: ((ElementErrorResponse?, String?) -> Unit)? = null
    var onClose: ((Activity) -> Unit)? = null
    var onCloseEvents: ((Activity) -> Unit)? = null
    var eventListener: ((JSONObject) -> Unit)? = null
}
