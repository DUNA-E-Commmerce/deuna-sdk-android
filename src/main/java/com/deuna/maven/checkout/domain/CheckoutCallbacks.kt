package com.deuna.maven.checkout.domain

import CheckoutResponse

class CheckoutCallbacks {
    var onSuccess: ((CheckoutResponse) -> Unit)? = null
    var onError: ((DeunaErrorMessage) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var eventListener: ((CheckoutEvent, CheckoutResponse) -> Unit)? = null
}
