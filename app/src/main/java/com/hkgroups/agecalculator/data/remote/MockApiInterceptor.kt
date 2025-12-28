package com.hkgroups.agecalculator.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Mock API Interceptor that simulates a real network backend.
 * 
 * Features:
 * - Intercepts all HTTP requests
 * - Simulates network latency (1.5 seconds)
 * - Returns mock JSON responses for zodiac endpoints
 * - Handles /signs (all signs) and /signs/{name} (single sign) endpoints
 * - Returns 404 for unknown endpoints
 * 
 * This allows full testing of the offline-first architecture without a real API.
 */
class MockApiInterceptor : Interceptor {

    companion object {
        private const val TAG = "MockApiInterceptor"
        private const val NETWORK_DELAY_MS = 1500L // 1.5 second delay to simulate network
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val path = url.encodedPath
        
        Log.d(TAG, "Intercepting request: $path")

        // Simulate network latency - crucial for seeing Loading state in UI
        Thread.sleep(NETWORK_DELAY_MS)

        return when {
            // Handle GET /signs - Return all zodiac signs
            path == "/signs" && request.method == "GET" -> {
                Log.d(TAG, "Returning all zodiac signs")
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(
                        MockResponseData.ALL_SIGNS_JSON
                            .toResponseBody("application/json".toMediaTypeOrNull())
                    )
                    .build()
            }

            // Handle GET /signs/{name} - Return specific zodiac sign
            path.startsWith("/signs/") && request.method == "GET" -> {
                val signName = path.substringAfterLast("/")
                Log.d(TAG, "Returning zodiac sign: $signName")
                
                val jsonResponse = MockResponseData.singleSignJson(signName)
                
                // Check if it's an error response (sign not found)
                val responseCode = if (jsonResponse.contains("\"error\"")) 404 else 200
                val message = if (responseCode == 200) "OK" else "Not Found"
                
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(responseCode)
                    .message(message)
                    .body(
                        jsonResponse.toResponseBody("application/json".toMediaTypeOrNull())
                    )
                    .build()
            }

            // Handle unknown endpoints - Return 404
            else -> {
                Log.w(TAG, "Unknown endpoint: $path")
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(404)
                    .message("Not Found")
                    .body(
                        """{"error": "Endpoint not found", "path": "$path"}"""
                            .toResponseBody("application/json".toMediaTypeOrNull())
                    )
                    .build()
            }
        }
    }
}
