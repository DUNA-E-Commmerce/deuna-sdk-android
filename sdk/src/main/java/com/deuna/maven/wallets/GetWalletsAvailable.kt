package com.deuna.maven.wallets

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.domain.UserInfo
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class GetWalletsAvailableParams(
    val orderToken: String? = null,
    val userInfo: UserInfo? = null,
)

fun DeunaSDK.getWalletsAvailable(
    context: Context,
    params: GetWalletsAvailableParams? = null,
    callback: (wallets: List<WalletProvider>, error: WalletsError?) -> Unit,
) {
    GetWalletsAvailable(
        context = context.applicationContext,
        environment = environment,
        publicApiKey = publicApiKey,
        params = params,
        callback = callback,
    ).run()
}

private class GetWalletsAvailable(
    private val context: Context,
    private val environment: Environment,
    private val publicApiKey: String,
    private val params: GetWalletsAvailableParams?,
    private val callback: (wallets: List<WalletProvider>, error: WalletsError?) -> Unit,
) {
    companion object {
        @Volatile
        private var cachedWallets: List<WalletProvider>? = null
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val workers = Executors.newSingleThreadExecutor()
    private val httpClient = OkHttpClient()

    fun run() {
        cachedWallets?.let {
            DeunaLogs.info("Returning cache")
            callbackOnMain(it, null)
            return
        }
        workers.execute {
            try {
                val backendWallets = fetch()
                val availableWallets = filterByDeviceAvailability(backendWallets)
                cachedWallets = availableWallets
                callbackOnMain(availableWallets, null)
            } catch (e: Exception) {
                DeunaLogs.error("[wallets] getWalletsAvailable failed: ${e.message}")
                callbackOnMain(emptyList(), WalletsError.fetchFailed(e.message ?: "Unknown error"))
            }
        }
    }

    private fun filterByDeviceAvailability(wallets: List<WalletProvider>): List<WalletProvider> {
        if (wallets.isEmpty()) return emptyList()
        return wallets.filter { provider ->
            when (provider) {
                WalletProvider.GOOGLE_PAY -> isGooglePayAvailable()
            }
        }
    }

    private fun isGooglePayAvailable(): Boolean {
        val gmsStatus = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context)
        if (gmsStatus != ConnectionResult.SUCCESS) {
            DeunaLogs.info("[wallets] Google Play Services unavailable (code=$gmsStatus). Google Pay excluded.")
            return false
        }

        val googlePayEnv = if (environment == Environment.PRODUCTION)
            WalletConstants.ENVIRONMENT_PRODUCTION
        else
            WalletConstants.ENVIRONMENT_TEST

        val paymentsClient = Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(googlePayEnv)
                .build()
        )

        val request = IsReadyToPayRequest.fromJson(
            JSONObject().apply {
                put("apiVersion", 2)
                put("apiVersionMinor", 0)
                put(
                    "allowedPaymentMethods", JSONArray().put(
                    JSONObject().apply {
                        put("type", "CARD")
                        put("parameters", JSONObject().apply {
                            put("allowedAuthMethods", JSONArray().apply {
                                put("PAN_ONLY")
                                put("CRYPTOGRAM_3DS")
                            })
                            put("allowedCardNetworks", JSONArray().apply {
                                put("AMEX")
                                put("DISCOVER")
                                put("MASTERCARD")
                                put("VISA")
                            })
                        })
                    }
                ))
            }.toString()
        )

        val latch = CountDownLatch(1)
        var isReady = false

        paymentsClient.isReadyToPay(request).addOnCompleteListener { task ->
            isReady = task.isSuccessful && task.result == true
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
        return isReady
    }

    private fun fetch(): List<WalletProvider> {
        val baseUrl = environment.elementsBaseUrl
        val urlBuilder = StringBuilder("$baseUrl/api/vault")
        params?.orderToken?.let {
            urlBuilder.append("?orderToken=${URLEncoder.encode(it, "UTF-8")}")
        }

        val request = Request.Builder()
            .url(urlBuilder.toString())
            .header("x-api-key", publicApiKey)
            .post(buildBody())
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }

        val responseBody = response.body?.string() ?: return emptyList()
        return parseWallets(responseBody)
    }

    private fun buildBody(): RequestBody {
        val email = params?.userInfo?.email
        if (params?.userInfo == null || email.isNullOrEmpty()) {
            return "".toRequestBody("application/json".toMediaType())
        }
        val json = JSONObject().apply {
            put("email", email)
            val firstName = params.userInfo.firstName
            val lastName = params.userInfo.lastName
            if (firstName.isNotEmpty()) put("firstName", firstName)
            if (lastName.isNotEmpty()) put("lastName", lastName)
        }
        return json.toString().toRequestBody("application/json".toMediaType())
    }

    private fun parseWallets(json: String): List<WalletProvider> {
        val root = JSONObject(json)
        val paymentMethods: JSONArray = root.optJSONArray("paymentMethods") ?: return emptyList()
        val result = mutableListOf<WalletProvider>()
        for (i in 0 until paymentMethods.length()) {
            val method = paymentMethods.optJSONObject(i) ?: continue
            val processorName = method.optString("processor_name")
            val provider = WalletProvider.fromProcessorName(processorName) ?: continue
            if (!result.contains(provider)) result.add(provider)
        }
        return result
    }

    private fun callbackOnMain(wallets: List<WalletProvider>, error: WalletsError?) {
        DeunaLogs.info("Deuna Wallets: ${wallets.size}")
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback(wallets, error)
        } else {
            mainHandler.post { callback(wallets, error) }
        }
    }
}
