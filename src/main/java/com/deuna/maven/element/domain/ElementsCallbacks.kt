package com.deuna.maven.element.domain

import ElementsResponse

class ElementsCallbacks {
    var onSuccess: ((ElementsResponse) -> Unit)? = null
    var onError: ((ElementsError) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var onCanceled: (() -> Unit)? = null
    var eventListener: ((ElementsEvent, ElementsResponse) -> Unit)? = null
}
