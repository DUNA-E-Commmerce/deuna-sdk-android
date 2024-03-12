package com.deuna.maven.checkout.domain

import CheckoutResponse

class CheckoutCallbacks {
    var onSuccess: ((CheckoutResponse) -> Unit)? = null
    var onError: ((CheckoutError) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var onCanceled: (() -> Unit)? = null
    var eventListener: ((CheckoutEvent, CheckoutResponse) -> Unit)? = null
}
