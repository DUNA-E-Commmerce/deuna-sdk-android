package com.deuna.sdkexample.integration.data.helpers

import com.deuna.sdkexample.integration.domain.CountryCode
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

// MARK: - Order Options

data class DeunanowOrderOptions(
    val orderId: String? = null,
    val storeCode: String? = null,
    val userEmail: String? = null,
    val amount: Int? = null
)

// MARK: - Country Data

data class CountryOrderData(
    val currency: String,
    val totalAmount: Int,
    val shippingAddress: ShippingAddressData,
    val billingAddress: BillingAddressData
)

data class ShippingAddressData(
    val phone: String,
    val identityDocument: String,
    val lat: Double,
    val lng: Double,
    val city: String,
    val zipcode: String,
    val stateName: String,
    val countryCode: String
)

data class BillingAddressData(
    val phone: String,
    val identityDocument: String,
    val address1: String,
    val address2: String,
    val city: String,
    val zipcode: String,
    val stateName: String,
    val country: String,
    val countryCode: String
)

// MARK: - Country Data Configuration

object CountryOrderDataConfig {
    val data: Map<CountryCode, CountryOrderData> = mapOf(
        CountryCode.MX to CountryOrderData(
            currency = "MXN",
            totalAmount = 10000,
            shippingAddress = ShippingAddressData(
                phone = "+525512345678",
                identityDocument = "ABCD123456HDFXYZ09",
                lat = 19.4326,
                lng = -99.1332,
                city = "Ciudad de México",
                zipcode = "06600",
                stateName = "CDMX",
                countryCode = "MX"
            ),
            billingAddress = BillingAddressData(
                phone = "+525512345678",
                identityDocument = "ABCD123456HDFXYZ09",
                address1 = "Av. Paseo de la Reforma 222",
                address2 = "Piso 10",
                city = "Ciudad de México",
                zipcode = "06600",
                stateName = "CDMX",
                country = "Mexico",
                countryCode = "MX"
            )
        ),
        CountryCode.CO to CountryOrderData(
            currency = "COP",
            totalAmount = 5000000,
            shippingAddress = ShippingAddressData(
                phone = "+573001234567",
                identityDocument = "1234567890",
                lat = 4.7110,
                lng = -74.0721,
                city = "Bogotá",
                zipcode = "110111",
                stateName = "Cundinamarca",
                countryCode = "CO"
            ),
            billingAddress = BillingAddressData(
                phone = "+573001234567",
                identityDocument = "1234567890",
                address1 = "Carrera 7 # 71-21",
                address2 = "Torre A Piso 5",
                city = "Bogotá",
                zipcode = "110111",
                stateName = "Cundinamarca",
                country = "Colombia",
                countryCode = "CO"
            )
        ),
        CountryCode.CL to CountryOrderData(
            currency = "CLP",
            totalAmount = 1000000,
            shippingAddress = ShippingAddressData(
                phone = "+56912345678",
                identityDocument = "12345678-9",
                lat = -33.4489,
                lng = -70.6693,
                city = "Santiago",
                zipcode = "8320000",
                stateName = "Región Metropolitana",
                countryCode = "CL"
            ),
            billingAddress = BillingAddressData(
                phone = "+56912345678",
                identityDocument = "12345678-9",
                address1 = "Av. Providencia 1234",
                address2 = "Oficina 501",
                city = "Santiago",
                zipcode = "8320000",
                stateName = "Región Metropolitana",
                country = "Chile",
                countryCode = "CL"
            )
        ),
        CountryCode.PE to CountryOrderData(
            currency = "PEN",
            totalAmount = 50000,
            shippingAddress = ShippingAddressData(
                phone = "+51912345678",
                identityDocument = "12345678",
                lat = -12.0464,
                lng = -77.0428,
                city = "Lima",
                zipcode = "15001",
                stateName = "Lima",
                countryCode = "PE"
            ),
            billingAddress = BillingAddressData(
                phone = "+51912345678",
                identityDocument = "12345678",
                address1 = "Av. Javier Prado Este 4200",
                address2 = "Piso 3",
                city = "Lima",
                zipcode = "15001",
                stateName = "Lima",
                country = "Peru",
                countryCode = "PE"
            )
        ),
        CountryCode.EC to CountryOrderData(
            currency = "USD",
            totalAmount = 10000,
            shippingAddress = ShippingAddressData(
                phone = "+593912345678",
                identityDocument = "1234567890",
                lat = -0.1807,
                lng = -78.4678,
                city = "Quito",
                zipcode = "170150",
                stateName = "Pichincha",
                countryCode = "EC"
            ),
            billingAddress = BillingAddressData(
                phone = "+593912345678",
                identityDocument = "1234567890",
                address1 = "Av. Amazonas N34-311",
                address2 = "Edificio Centro",
                city = "Quito",
                zipcode = "170150",
                stateName = "Pichincha",
                country = "Ecuador",
                countryCode = "EC"
            )
        )
    )
}

// MARK: - Response Model

data class TokenizeOrderResponse(
    val token: String,
    val orderType: String
) {
    companion object {
        fun fromJson(json: JSONObject): TokenizeOrderResponse {
            return TokenizeOrderResponse(
                token = json.getString("token"),
                orderType = json.getString("order_type")
            )
        }
    }
}

// MARK: - Order Builder

object DeunanowOrderBuilder {

    /**
     * Creates a DEUNA_NOW order with country-specific data
     * @param country The country code for localization
     * @param options Optional customizations for the order
     * @return JSONObject ready to be sent to the API, or null if country is unsupported
     */
    fun createOrder(
        country: CountryCode,
        options: DeunanowOrderOptions = DeunanowOrderOptions()
    ): JSONObject? {
        val countryData = CountryOrderDataConfig.data[country] ?: run {
            android.util.Log.e("DeunanowOrderBuilder", "❌ Unsupported country code: $country")
            return null
        }

        val orderId = options.orderId ?: UUID.randomUUID().toString()
        val storeCode = options.storeCode ?: "all"
        val userEmail = options.userEmail ?: "test-${UUID.randomUUID()}@test.com"
        val finalAmount = options.amount ?: countryData.totalAmount
        val displayAmount = "${countryData.currency} ${String.format("%.2f", finalAmount / 100.0)}"

        val shippingAddress = JSONObject().apply {
            put("first_name", "QA Test")
            put("last_name", "Automation")
            put("phone", countryData.shippingAddress.phone)
            put("identity_document", countryData.shippingAddress.identityDocument)
            put("lat", countryData.shippingAddress.lat)
            put("lng", countryData.shippingAddress.lng)
            put("address1", "Test Address 123")
            put("address2", "")
            put("city", countryData.shippingAddress.city)
            put("zipcode", countryData.shippingAddress.zipcode)
            put("state_name", countryData.shippingAddress.stateName)
            put("country", countryData.billingAddress.country)
            put("country_code", countryData.shippingAddress.countryCode)
            put("email", userEmail)
        }

        val billingAddress = JSONObject().apply {
            put("first_name", "QA Test")
            put("last_name", "Automation")
            put("phone", countryData.billingAddress.phone)
            put("identity_document", countryData.billingAddress.identityDocument)
            put("lat", countryData.shippingAddress.lat)
            put("lng", countryData.shippingAddress.lng)
            put("address1", countryData.billingAddress.address1)
            put("address2", countryData.billingAddress.address2)
            put("city", countryData.billingAddress.city)
            put("zipcode", countryData.billingAddress.zipcode)
            put("state_name", countryData.billingAddress.stateName)
            put("country", countryData.billingAddress.country)
            put("country_code", countryData.billingAddress.countryCode)
            put("email", userEmail)
        }

        val item = JSONObject().apply {
            put("id", "79")
            put("name", "Test Product")
            put("description", "Test product description")
            put("quantity", 1)
            put("sku", "SKU-11021")
            put("category", "test")
            put("total_amount", JSONObject().apply {
                put("amount", finalAmount)
                put("display_amount", displayAmount)
            })
            put("unit_price", JSONObject().apply {
                put("amount", finalAmount)
                put("display_amount", displayAmount)
            })
        }

        val order = JSONObject().apply {
            put("order_id", orderId)
            put("store_code", storeCode)
            put("currency", countryData.currency)
            put("tax_amount", finalAmount)
            put("shipping_amount", finalAmount)
            put("items_total_amount", finalAmount)
            put("sub_total", finalAmount)
            put("total_amount", finalAmount)
            put("display_total_amount", displayAmount)
            put("items", JSONArray().put(item))
            put("discounts", JSONArray())
            put("shipping_address", shippingAddress)
            put("billing_address", billingAddress)
            put("status", "pending")
            put("timezone", "America/Mexico_City")
        }

        return JSONObject().apply {
            put("order_type", "DEUNA_NOW")
            put("order", order)
        }
    }
}
