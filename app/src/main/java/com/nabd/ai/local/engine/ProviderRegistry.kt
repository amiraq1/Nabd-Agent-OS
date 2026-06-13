package com.nabd.ai.local.engine

/**
 * ProviderRegistry: Manages registration and retrieval of LLM providers.
 */
interface ProviderRegistry {
    fun register(provider: LlmProvider)
    fun get(providerId: String): LlmProvider
    fun getOrNull(providerId: String): LlmProvider?
    fun isRegistered(providerId: String): Boolean
    fun getAll(): List<LlmProvider>
}

class DefaultProviderRegistry : ProviderRegistry {
    private val providers = mutableMapOf<String, LlmProvider>()

    override fun register(provider: LlmProvider) {
        providers[provider.id] = provider
    }

    override fun get(providerId: String): LlmProvider {
        return providers[providerId] ?: throw IllegalArgumentException("Provider $providerId not found. Available: ${providers.keys}")
    }

    override fun getOrNull(providerId: String): LlmProvider? {
        return providers[providerId]
    }

    override fun isRegistered(providerId: String): Boolean {
        return providers.containsKey(providerId)
    }

    override fun getAll(): List<LlmProvider> {
        return providers.values.toList()
    }
}

