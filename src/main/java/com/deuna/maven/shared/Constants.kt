package com.deuna.maven.shared

import com.deuna.maven.element.domain.ElementsError

enum class ErrorCodes {
    INITIALIZATION_ERROR,
    UNKNOWN_ERROR
}

enum class ErrorMessages(val message: String) {
    UNKNOWN("Unknown error")
}

enum class PaymentsErrorMessages(val message: String) {
    ORDER_TOKEN_MUST_NOT_BE_EMPTY("OrderToken must not be empty."),
    PAYMENT_LINK_COULD_NOT_BE_GENERATED("Payment link could not be generated."),
    NO_INTERNET_CONNECTION("No internet connection available."),
    ORDER_COULD_NOT_BE_RETRIEVED("Order could not be retrieved.");
}

class PaymentWidgetErrors {
    companion object {
        val noInternetConnection = PaymentsError(
            type = PaymentsError.Type.NO_INTERNET_CONNECTION
        )

        val invalidOrderToken = PaymentsError(
            type = PaymentsError.Type.INVALID_ORDER_TOKEN,
            metadata = PaymentsError.Metadata(
                code = ErrorCodes.INITIALIZATION_ERROR.name,
                message = PaymentsErrorMessages.ORDER_TOKEN_MUST_NOT_BE_EMPTY.message
            )
        )

        val linkCouldNotBeGenerated = PaymentsError(
            type = PaymentsError.Type.INITIALIZATION_FAILED,
            metadata = PaymentsError.Metadata(
                code = ErrorCodes.INITIALIZATION_ERROR.name,
                message = PaymentsErrorMessages.PAYMENT_LINK_COULD_NOT_BE_GENERATED.message
            )
        )
    }
}


class ElementsErrors {
    companion object {
        val noInternetConnection = ElementsError(
            type = ElementsError.Type.NO_INTERNET_CONNECTION
        )
    }
}