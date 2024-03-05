package com.deuna.maven


import com.deuna.maven.shared.Environment
import java.lang.IllegalStateException


/**
 * Class representing the Deuna SDK.
 *
 * @property environment The Deuna environment (Environment.PRODUCTION, Environment.DEVELOPMENT, etc).
 * @property publicApiKey The public API key to access Deuna services (for elements operations).
 * @property privateApiKey The private API key to access Deuna services (for checkout operations).
 */
open class DeunaSDK(
    val environment: Environment,
    val publicApiKey: String,
    val privateApiKey: String,
) {

    init {
        require(publicApiKey.isNotEmpty() && privateApiKey.isNotEmpty()) {
            "Public and private API keys must not be empty"
        }
    }


    companion object {
        // Unique instance of the Deuna SDK
        private var instance: DeunaSDK? = null

        /**
         * Gets the shared instance of the Deuna SDK.
         *
         * @throws IllegalStateException if DeunaSDK.initializeSingleton is not called before accessing this instance.
         * @return The same instance of DeunaSDK
         */
        val shared: DeunaSDK
            get() {
                return instance ?: throw IllegalStateException(
                    "DeunaSDK.initializeSingleton must be called before accessing shared instance"
                )
            }

        /**
         * Registers an unique instance of the Deuna SDK.
         *
         * @param environment The Deuna environment (Environment.PRODUCTION, Environment.DEVELOPMENT, etc).
         * @param publicApiKey The public API key to access Deuna services.
         * @param privateApiKey The private API key to access Deuna services.
         */
        fun initializeSingleton(
            environment: Environment,
            publicApiKey: String,
            privateApiKey: String,
        ): DeunaSDK {
            instance = DeunaSDK(environment, publicApiKey, privateApiKey)
            return instance!!
        }
    }
}