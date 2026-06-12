package com.nabd.ai.local.core

import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor: Centralized credential injection for cloud LLM providers.
 */
class AuthInterceptor(
    private val keyManager: SecureKeyManager, 
    private val providerId: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val apiKey = keyManager.getKey(providerId) ?: return chain.proceed(originalRequest)

        val requestBuilder = originalRequest.newBuilder()
        
        when (providerId.lowercase()) {
            "openai" -> {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }
            "gemini" -> {
                // Gemini supports both query parameter and header.
                // We'll use the header to keep the URL clean.
                requestBuilder.addHeader("x-goog-api-key", apiKey)
            }
            "anthropic" -> {
                requestBuilder.addHeader("x-api-key", apiKey)
                requestBuilder.addHeader("anthropic-version", "2023-06-01")
            }
            else -> {
                // Default to Bearer token
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
