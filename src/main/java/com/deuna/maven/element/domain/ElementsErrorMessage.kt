package com.deuna.maven.element.domain

import ElementsResponse

data class ElementsErrorMessage(var message: String,
                                var type: String,
                                var order: ElementsResponse.Order,
                                var user: ElementsResponse.User)