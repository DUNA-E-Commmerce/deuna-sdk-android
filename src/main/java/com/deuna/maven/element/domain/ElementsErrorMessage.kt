package com.deuna.maven.element.domain

import ElementResponse

data class ElementsErrorMessage(var message: String,
                                var type: String,
                                var order: ElementResponse.Order,
                                var user: ElementResponse.User)