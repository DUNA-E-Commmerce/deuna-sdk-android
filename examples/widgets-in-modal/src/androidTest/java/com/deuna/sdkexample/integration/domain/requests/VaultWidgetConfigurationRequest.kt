package com.deuna.sdkexample.integration.domain.requests

import org.json.JSONArray
import org.json.JSONObject

/**
 * Mirrors the vault configuration payload used by e2e Playwright tests
 * (configureVaultWidget -> createCheckoutConfigVault default values).
 */
object VaultWidgetConfigurationRequest {

    fun checkoutConfiguration(): JSONObject = JSONObject().apply {
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
    }

    fun checkoutV2Configuration(render3dsStrategy: String? = null): JSONObject = JSONObject().apply {
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
                put("minor_units", JSONArray())
            })
            put("checkout_modules", JSONArray())
            render3dsStrategy?.let { put("render_3ds_strategy", it) }
        })
    }

    fun createNetworkRequestBody(): JSONObject = JSONObject().apply {
        put("name", "network_${java.util.UUID.randomUUID()}")
        put("is_private", true)
    }
}
