package com.deuna.maven.element.domain

import ElementResponse

class ElementsCallbacks {
    var onSuccess: ((ElementResponse) -> Unit)? = null
    var onError: ((ElementsErrorMessage?) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var eventListener: ((ElementsEvent, ElementResponse) -> Unit)? = null
}
