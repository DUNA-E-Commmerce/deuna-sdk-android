package com.deuna.sdkexample.integration.domain.responses

import org.json.JSONObject

data class MerchantResponse(
    val id: String,
    val country: String,
    val city: String,
    val name: String,
    val shortName: String,
    val timezone: String,
    val managedByDeuna: Boolean,
    val currency: String,
    val termAndConditionsUrl: String,
    val createdAt: String,
    val updatedAt: String,
    val privacyPoliciesUrl: String,
    val tenantId: String,
    val externalMerchantActivityCode: String
) {
    companion object {
        fun fromJson(json: JSONObject): MerchantResponse {
            return MerchantResponse(
                id = json.getString("id"),
                country = json.getString("country"),
                city = json.getString("city"),
                name = json.getString("name"),
                shortName = json.getString("short_name"),
                timezone = json.getString("timezone"),
                managedByDeuna = json.getBoolean("managed_by_duna"),
                currency = json.getString("currency"),
                termAndConditionsUrl = json.optString("term_and_conditions_url", ""),
                createdAt = json.getString("created_at"),
                updatedAt = json.getString("updated_at"),
                privacyPoliciesUrl = json.optString("privacy_policies_url", ""),
                tenantId = json.optString("tenant_id", ""),
                externalMerchantActivityCode = json.optString("external_merchant_activity_code", "")
            )
        }
    }
}
