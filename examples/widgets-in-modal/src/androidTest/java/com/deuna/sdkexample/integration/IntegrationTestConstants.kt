package com.deuna.sdkexample.integration

import com.deuna.sdkexample.integration.core.TestEnvironment
import com.deuna.sdkexample.integration.domain.CountryCode

object IntegrationTestConstants {
    // Use environment variable if set, otherwise default to STAGING.
    val env: TestEnvironment = TestEnvironment.fromEnvironment()
    val country: CountryCode = CountryCode.MX
}
