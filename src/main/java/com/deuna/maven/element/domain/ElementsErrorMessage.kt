package com.deuna.maven.element.domain

import ElementsResponse
import com.deuna.maven.shared.*

data class ElementsErrorMessage(
    var type: DeunaSDKError,
    var order: ElementsResponse.Order?,
    var user: ElementsResponse.User?
)

