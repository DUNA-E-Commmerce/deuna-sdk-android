package com.deuna.compose_demo.screens

import CheckoutResponse
import ElementsResponse
import com.deuna.maven.checkout.domain.CheckoutError
import com.deuna.maven.element.domain.ElementsError



/**
 * Sealed classes for representing the results of checkout and element saving processes.
 */

sealed class PaymentWidgetResult {

    /**
     * Indicates successful completion of the payment process.
     *
     * @property response The CheckoutResponse.Dat object containing details about the completed payment
     */
    data class Success(val response: CheckoutResponse.Data) : PaymentWidgetResult()

    /**
     * Indicates an error occurred during the payment process.
     *
     */
    data object Error : PaymentWidgetResult()

    /**
     * Indicates the payment process was cancelled by the user.
     */
    data object Canceled : PaymentWidgetResult()
}


/**
 * Sealed classes for representing the results of checkout and element saving processes.
 */

sealed class CheckoutResult {

    /**
     * Indicates successful completion of the checkout process.
     *
     * @property response The CheckoutResponse object containing details about the completed checkout.
     */
    data class Success(val response: CheckoutResponse) : CheckoutResult()

    /**
     * Indicates an error occurred during the checkout process.
     *
     * @property error The DeunaErrorMessage object detailing the error encountered.
     */
    data class Error(val error: CheckoutError) : CheckoutResult()

    /**
     * Indicates the checkout process was cancelled by the user.
     */
    data object Canceled : CheckoutResult()
}


sealed class ElementsResult {

    /**
     * Indicates successful completion of the element saving process (e.g., saving card information).
     *
     * @property response The ElementsResponse object containing details about the saved elements.
     */
    data class Success(val response: ElementsResponse) : ElementsResult()

    /**
     * Indicates an error occurred during the element saving process.
     *
     * @property error The ElementsErrorMessage object detailing the error encountered.
     */
    data class Error(val error: ElementsError) : ElementsResult()

    /**
     * Indicates the element saving process was cancelled by the user.
     */
    data object Canceled : ElementsResult()
}