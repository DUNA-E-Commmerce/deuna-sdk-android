package com.deuna.maven.element.domain

import ElementsResponse

class ElementsCallbacks {
    var onSuccess: ((ElementsResponse) -> Unit)? = null
    var onError: ((ElementsErrorMessage?) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var eventListener: ((ElementsEvent, ElementsResponse) -> Unit)? = null
}
