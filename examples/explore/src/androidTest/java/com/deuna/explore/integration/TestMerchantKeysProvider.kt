package com.deuna.explore.integration

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

enum class TestTargetEnvironment(val drawerTitle: String) {
    SANDBOX("Sandbox"),
    DEVELOP("Develop"),
    STAGING("Staging"),
}

enum class TestProcessorType {
    STRIPE_AUTHORIZE,
    STRIPE_3DS,
    PAYU_EFECTY,
}

data class TestMerchantSetup(
    val processorType: TestProcessorType = TestProcessorType.STRIPE_AUTHORIZE,
    val countryIso: String = "MX",
)

data class TestMerchantKeys(
    val publicKey: String,
    val privateKey: String,
    val targetEnvironment: TestTargetEnvironment,
)

object TestMerchantKeysProvider {
    private val baseUrl: String = System.getenv("DEUNA_API_ENDPOINT") ?: "https://api.stg.deuna.io"
    private val adminUsername: String = System.getenv("ADMIN_USERNAME") ?: "developers@getduna.com"
    private val adminPassword: String = System.getenv("ADMIN_PASSWORD") ?: "superadmin"

    fun createKeys(setup: TestMerchantSetup = TestMerchantSetup()): TestMerchantKeys {
        val country = setup.countryIso.uppercase()
        val city = if (country == "CO") "Bogota" else "CDMX"
        val timezone = if (country == "CO") "America/Bogota" else "America/Mexico_City"
        val currency = if (country == "CO") "COP" else "MXN"

        val merchant = requestJson(
            path = "/merchants",
            method = "POST",
            body = JSONObject().apply {
                put("country", country)
                put("city", city)
                put("name", "AUTO_MERCHANT_${country} ${Random.nextInt(10000, 9009999)}")
                put("short_name", "AUTO")
                put("timezone", timezone)
                put("currency", currency)
                put("managed_by_deuna", false)
            }
        )
        val merchantId = merchant.getString("id")

        val login = requestJson(
            path = "/merchants/login",
            method = "POST",
            body = JSONObject().apply {
                put("username", adminUsername)
                put("password", adminPassword)
            }
        )
        val merchantToken = login.getString("token")

        requestJson(
            path = "/merchants/$merchantId/stores",
            method = "POST",
            body = JSONObject().apply {
                put("name", "all")
                put("address", "Auto generated address")
                put("latitude", 19.3600265)
                put("longitude", -99.1574174)
                put("is_default", true)
            },
            headers = mapOf("Authorization" to "Bearer $merchantToken")
        )

        val app = requestJson(
            path = "/merchants/$merchantId/applications",
            method = "POST",
            body = JSONObject().apply {
                put("name", "ExploreAuto")
                put("is_sandbox", true)
                put("expires_at", "DEV")
            },
            headers = mapOf("Authorization" to "Bearer $merchantToken")
        )

        configureVaultWidget(merchantId = merchantId, merchantToken = merchantToken)
        createPaymentProcessor(
            merchantId = merchantId,
            merchantToken = merchantToken,
            processorType = setup.processorType,
            countryIso = country,
            currencyIso3 = currency,
        )

        return TestMerchantKeys(
            publicKey = app.getString("public_key"),
            privateKey = app.getString("private_key"),
            targetEnvironment = targetEnvironmentFromBaseUrl(),
        )
    }

    fun targetEnvironmentFromBaseUrl(): TestTargetEnvironment {
        val url = baseUrl.lowercase()
        return when {
            "sandbox" in url -> TestTargetEnvironment.SANDBOX
            "dev" in url -> TestTargetEnvironment.DEVELOP
            else -> TestTargetEnvironment.STAGING
        }
    }

    private fun requestJson(
        path: String,
        method: String,
        body: JSONObject? = null,
        headers: Map<String, String> = emptyMap(),
    ): JSONObject {
        val conn = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Content-Type", "application/json")
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
            doInput = true
            if (body != null && method != "GET") {
                doOutput = true
                OutputStreamWriter(outputStream).use {
                    it.write(body.toString())
                    it.flush()
                }
            }
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = BufferedReader(InputStreamReader(stream)).use { it.readText() }
        if (code !in 200..299) {
            throw AssertionError("API call failed ($code) $path: $response")
        }
        return JSONObject(response)
    }

    private fun configureVaultWidget(merchantId: String, merchantToken: String) {
        requestJson(
            path = "/checkout/$merchantId/configuration",
            method = "POST",
            body = JSONObject().apply {
                put("configuration", JSONObject().apply {
                    put("exclude_billing_address", false)
                    put("is_colorblind", false)
                    put("is_identity_document_hide", false)
                    put("hide_pickup_time", false)
                })
                put("image_url", "")
                put("theme", JSONObject().apply {
                    put("main_color", "#5F529E")
                    put("secondary_color", "#d9d4d4")
                    put("background_color", "#FFFFFF")
                })
            },
            headers = mapOf("Authorization" to "Bearer $merchantToken")
        )

        requestJson(
            path = "/checkout-config/merchants/$merchantId/configurations",
            method = "POST",
            body = JSONObject().apply {
                put("order_config", JSONObject().apply {
                    put("notify_type", "async")
                    put("payment_link", JSONObject().apply {
                        put("generate_user_auth_token", true)
                    })
                })
                put("elements_config", JSONObject().apply {
                    put("init_with_guest_user", true)
                    put("user_authentication_flow", false)
                })
                put("widgets_general_config", JSONObject().apply {
                    put("theme", JSONObject().apply {
                        put("main_color", "")
                        put("secondary_color", "")
                        put("background_color", "")
                        put("font", "")
                        put("image", JSONObject().apply { put("name", "") })
                        put("banner", "")
                        put("main_action_button_text", "")
                    })
                    put("user_experience", JSONObject().apply {
                        put("show_saved_cards_flow", false)
                        put("disable_login", true)
                    })
                    put("flags", JSONObject())
                    put("currencies", JSONObject().apply {
                        put("minor_units", org.json.JSONArray())
                    })
                    put("checkout_modules", org.json.JSONArray())
                })
            },
            headers = mapOf("Authorization" to "Bearer $merchantToken")
        )

        val network = requestJson(
            path = "/users/networks",
            method = "POST",
            body = JSONObject().apply {
                put("name", "network_${java.util.UUID.randomUUID()}")
                put("is_private", true)
            },
            headers = mapOf("Authorization" to "Bearer $merchantToken")
        )
        val networkId = network.getString("id")

        requestJson(
            path = "/users/networks/$networkId/merchants/$merchantId",
            method = "POST",
            headers = mapOf("Authorization" to "Bearer $merchantToken")
        )
    }

    private fun createPaymentProcessor(
        merchantId: String,
        merchantToken: String,
        processorType: TestProcessorType,
        countryIso: String,
        currencyIso3: String,
    ) {
        val body = when (processorType) {
            TestProcessorType.STRIPE_AUTHORIZE -> JSONObject().apply {
                put("name", "STRIPE_DIRECT")
                put("payment_processor_id", 50)
                put("enabled", true)
                put("currency_iso3", currencyIso3)
                put("country_iso", countryIso)
                put("public_api_key", "pk_test_51KanPjHzvAJD6fk5wekkuEqfrT0qyzDrNw9Rlw2Nu4PBqlS8Vt1FtTgVDmXZxZ0eM4ccxBarVwUa5v0TICjSOyei00HmiJm3my")
                put("private_api_key", "sk_test_51KanPjHzvAJD6fk5iA6c7M6Hv9pHZZirj5kzEUzGW7x5Xh4bTXIbs9kxOxdawiOQxiDoySOiRDOmtyqAm3loCQmZ001dBHZhy0")
                put("allow_installments", true)
                put("enable_3ds_authentication", false)
                put("automatic_capture", false)
            }
            TestProcessorType.STRIPE_3DS -> JSONObject().apply {
                put("name", "STRIPE_DIRECT")
                put("payment_processor_id", 50)
                put("enabled", true)
                put("currency_iso3", currencyIso3)
                put("country_iso", countryIso)
                put("public_api_key", "pk_test_51KanPjHzvAJD6fk5wekkuEqfrT0qyzDrNw9Rlw2Nu4PBqlS8Vt1FtTgVDmXZxZ0eM4ccxBarVwUa5v0TICjSOyei00HmiJm3my")
                put("private_api_key", "sk_test_51KanPjHzvAJD6fk5iA6c7M6Hv9pHZZirj5kzEUzGW7x5Xh4bTXIbs9kxOxdawiOQxiDoySOiRDOmtyqAm3loCQmZ001dBHZhy0")
                put("allow_installments", true)
                put("enable_3ds_authentication", true)
                put("automatic_capture", true)
            }
            TestProcessorType.PAYU_EFECTY -> JSONObject().apply {
                put("name", "PAYU_EFECTY")
                put("payment_processor_id", 58)
                put("enabled", true)
                put("currency_iso3", "COP")
                put("country_iso", "CO")
                put("external_merchant_id", "511620")
                put("private_api_key", "bjuSxu382vh5d5GjxpRu409wQ2")
                put("public_api_key", "jI5ZhgfpWpVoM15")
                put("automatic_capture", true)
                put("flow", "payconnect")
                put("extra_params", JSONObject().apply {
                    put("account_id", "516553")
                })
            }
        }

        requestJson(
            path = "/merchants/$merchantId/stores/all/processors",
            method = "POST",
            body = body,
            headers = mapOf("Authorization" to "Bearer $merchantToken")
        )
    }
}
