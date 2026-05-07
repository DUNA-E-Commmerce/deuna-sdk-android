package com.deuna.explore.data

import com.deuna.explore.domain.ExploreProduct
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

object ProductCatalog {
    private data class CurrencyProfile(
        val code: String,
        val symbol: String,
        val rateFromUSD: Double,
        val decimalDigits: Int,
    )

    private data class ProductSeed(
        val id: String,
        val name: String,
        val image: String,
        val baseUSDPrice: Double,
    )

    private val seeds = listOf(
        ProductSeed("polo-shirt", "Polo Shirt", "polo_shirt", 105.55),
        ProductSeed("headphones", "Headphones", "headphones", 151.00),
        ProductSeed("sun-glasses", "Sun Glasses", "sun_glasses", 50.00),
    )

    private val fallback = CurrencyProfile("USD", "$", 1.0, 2)

    private val currencies = mapOf(
        "USD" to CurrencyProfile("USD", "$", 1.0, 2),
        "MXN" to CurrencyProfile("MXN", "$", 17.0, 2),
        "COP" to CurrencyProfile("COP", "$", 3900.0, 0),
        "CLP" to CurrencyProfile("CLP", "$", 950.0, 0),
        "PEN" to CurrencyProfile("PEN", "S/", 3.75, 2),
        "BRL" to CurrencyProfile("BRL", "R$", 5.1, 2),
    )

    fun buildProducts(currencyCode: String): List<ExploreProduct> {
        val profile = currencies[currencyCode.uppercase()] ?: fallback
        return seeds.map { seed ->
            val converted = seed.baseUSDPrice * profile.rateFromUSD
            val multiplier = if (profile.decimalDigits == 0) 1 else 100
            val cents = (converted * multiplier).roundToInt()
            ExploreProduct(
                id = seed.id,
                name = seed.name,
                image = seed.image,
                priceInCents = cents,
                fractionDigits = profile.decimalDigits,
                currencyCode = profile.code,
                currencySymbol = profile.symbol,
            )
        }
    }

    fun formatPrice(cents: Int, fractionDigits: Int, currencySymbol: String): String {
        val divisor = if (fractionDigits == 0) 1.0 else 100.0
        val value = cents / divisor
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = fractionDigits
            maximumFractionDigits = fractionDigits
        }
        return "$currencySymbol ${formatter.format(value)}"
    }

    fun fallbackMerchantProfile() = com.deuna.explore.domain.ExploreMerchantProfile(
        name = "", countryCode = "US", currencyCode = "USD"
    )
}
