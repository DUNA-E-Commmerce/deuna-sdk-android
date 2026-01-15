package com.deuna.sdkexample.integration.core

/**
 * Represents the test environment configuration.
 */
enum class TestEnvironment(val value: String) {
    STAGING("staging"),
    DEVELOPMENT("development");

    /**
     * Returns the API endpoint URL for the environment.
     */
    val apiEndpoint: String
        get() = when (this) {
            STAGING -> "https://api.stg.deuna.io"
            DEVELOPMENT -> "https://api.dev.deuna.io"
        }
}
