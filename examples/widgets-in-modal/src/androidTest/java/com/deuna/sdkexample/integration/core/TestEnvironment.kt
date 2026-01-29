package com.deuna.sdkexample.integration.core

/**
 * Represents the test environment configuration.
 */
enum class TestEnvironment(val value: String) {
    STAGING("staging"),
    DEVELOPMENT("development"),
    PREPROD("preprod");

    /**
     * Returns the API endpoint URL for the environment.
     * For PREPROD, it reads from the DEUNA_API_ENDPOINT environment variable.
     */
    val apiEndpoint: String
        get() = when (this) {
            STAGING -> "https://api.stg.deuna.io"
            DEVELOPMENT -> "https://api.dev.deuna.io"
            PREPROD -> System.getenv("DEUNA_API_ENDPOINT") ?: "http://apigw:8080"
        }

    companion object {
        /**
         * Gets the test environment from the DEUNA_ENV environment variable.
         * Defaults to STAGING if not set or invalid.
         */
        fun fromEnvironment(): TestEnvironment {
            val envValue = System.getenv("DEUNA_ENV")?.lowercase()
            return entries.find { it.value == envValue } ?: STAGING
        }
    }
}
}
