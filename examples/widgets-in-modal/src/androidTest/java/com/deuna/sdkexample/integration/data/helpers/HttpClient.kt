package com.deuna.sdkexample.integration.data.helpers

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * HTTP methods supported by the client
 */
enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH
}

/**
 * A simple HTTP client for making API requests
 */
class HttpClient(private val baseUrl: String) {

    companion object {
        private const val TAG = "HttpClient"
    }

    /**
     * Makes an HTTP request and returns the response as JSONObject.
     * @param path The API endpoint path (e.g., "/merchants").
     * @param method The HTTP method to use.
     * @param body Optional JSON body to send with the request.
     * @param headers Optional additional headers.
     * @param completion Callback with the result or error.
     */
    fun request(
        path: String,
        method: HttpMethod = HttpMethod.GET,
        body: JSONObject? = null,
        headers: Map<String, String>? = null,
        completion: (Result<JSONObject>) -> Unit
    ) {
        Thread {
            try {
                val url = URL("$baseUrl$path")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = method.name
                connection.setRequestProperty("Content-Type", "application/json")

                headers?.forEach { (key, value) ->
                    connection.setRequestProperty(key, value)
                }

                if (body != null && method != HttpMethod.GET) {
                    connection.doOutput = true
                    OutputStreamWriter(connection.outputStream).use { writer ->
                        writer.write(body.toString())
                        writer.flush()
                    }
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "📥 Status Code: $responseCode")

                val inputStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }

                Log.d(TAG, "📥 Response: $response")

                if (responseCode in 200..299) {
                    val jsonResponse = JSONObject(response)
                    completion(Result.success(jsonResponse))
                } else {
                    completion(Result.failure(HttpClientError.HttpError(responseCode, response)))
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Network error: ${e.message}")
                completion(Result.failure(e))
            }
        }.start()
    }

    /**
     * Makes a synchronous HTTP request.
     * @param path The API endpoint path.
     * @param method The HTTP method to use.
     * @param body Optional JSON body.
     * @param headers Optional additional headers.
     * @param timeout Maximum time to wait in seconds. Defaults to 30 seconds.
     * @return The JSON response.
     * @throws HttpClientError if the request fails or times out.
     */
    @Throws(HttpClientError::class)
    fun requestSync(
        path: String,
        method: HttpMethod = HttpMethod.GET,
        body: JSONObject? = null,
        headers: Map<String, String>? = null,
        timeout: Long = 30
    ): JSONObject {
        var result: JSONObject? = null
        var requestError: Throwable? = null
        val latch = CountDownLatch(1)

        request(path, method, body, headers) { response ->
            response.fold(
                onSuccess = { result = it },
                onFailure = { requestError = it }
            )
            latch.countDown()
        }

        val completed = latch.await(timeout, TimeUnit.SECONDS)

        if (!completed) {
            throw HttpClientError.Timeout
        }

        requestError?.let { throw it }

        return result ?: throw HttpClientError.NoData
    }
}

/**
 * Errors that can occur during HTTP requests
 */
sealed class HttpClientError : Exception() {
    object InvalidUrl : HttpClientError() {
        private fun readResolve(): Any = InvalidUrl
        override val message: String = "Invalid URL"
    }

    object NoData : HttpClientError() {
        private fun readResolve(): Any = NoData
        override val message: String = "No data received"
    }

    object Timeout : HttpClientError() {
        private fun readResolve(): Any = Timeout
        override val message: String = "Request timed out"
    }

    data class HttpError(val statusCode: Int, val responseBody: String) : HttpClientError() {
        override val message: String = "HTTP Error $statusCode: $responseBody"
    }
}
