package com.nabd.ai.local.engine

import android.util.Log

/**
 * ProviderMetrics: Captures performance and usage data for LLM providers.
 */
class ProviderMetrics {

    fun recordSuccess(providerId: String, modelId: String, latencyMs: Long, tokens: Int) {
        Log.i("ProviderMetrics", "SUCCESS | Provider: $providerId | Model: $modelId | Latency: ${latencyMs}ms | Tokens: $tokens")
        // In a real app, this would send data to Firebase, Sentry, or a local database.
    }

    fun recordFailure(providerId: String, modelId: String, throwable: Throwable) {
        Log.e("ProviderMetrics", "FAILURE | Provider: $providerId | Model: $modelId | Error: ${throwable.message}")
    }

    fun recordLatency(providerId: String, modelId: String, latencyMs: Long) {
        Log.d("ProviderMetrics", "LATENCY | Provider: $providerId | Model: $modelId | Latency: ${latencyMs}ms")
    }
}
