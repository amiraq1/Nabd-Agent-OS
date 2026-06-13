package com.nabd.ai.local.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProviderRegistryTest {

    private lateinit var registry: DefaultProviderRegistry

    @Before
    fun setUp() {
        registry = DefaultProviderRegistry()
    }

    // ── Helper: Stub Provider ───────────────────────────────

    private class StubProvider(
        override val id: String,
        override val isLocal: Boolean = false
    ) : LlmProvider {
        override val name: String = id
        override val state: kotlinx.coroutines.flow.StateFlow<EngineState> = kotlinx.coroutines.flow.MutableStateFlow(EngineState.Ready)
        override suspend fun initialize(config: ProviderConfig) {}
        override suspend fun shutdown() {}
        override suspend fun generateText(request: GenerationRequest): kotlinx.coroutines.flow.Flow<GenerationChunk> {
            return kotlinx.coroutines.flow.flow {
                emit(GenerationChunk("text"))
            }
        }
    }

    // ── Registration & Retrieval ────────────────────────────

    @Test
    fun `register and get provider`() {
        val provider = StubProvider("test_provider")
        registry.register(provider)
        
        val retrieved = registry.get("test_provider")
        assertEquals("test_provider", retrieved.id)
    }

    @Test
    fun `getOrNull returns provider when registered`() {
        val provider = StubProvider("openai")
        registry.register(provider)
        
        assertNotNull(registry.getOrNull("openai"))
        assertEquals("openai", registry.getOrNull("openai")?.id)
    }

    @Test
    fun `getOrNull returns null when not registered`() {
        assertNull(registry.getOrNull("nonexistent"))
    }

    @Test
    fun `isRegistered returns true for registered provider`() {
        registry.register(StubProvider("gemini"))
        assertTrue(registry.isRegistered("gemini"))
    }

    @Test
    fun `isRegistered returns false for unregistered provider`() {
        assertFalse(registry.isRegistered("nonexistent"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `get throws for unregistered provider`() {
        registry.get("nonexistent")
    }

    @Test
    fun `get error message lists available providers`() {
        registry.register(StubProvider("openai"))
        registry.register(StubProvider("gemini"))
        
        try {
            registry.get("anthropic")
            fail("Should have thrown")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("openai"))
            assertTrue(e.message!!.contains("gemini"))
        }
    }

    // ── GetAll ───────────────────────────────────────────────

    @Test
    fun `getAll returns all registered providers`() {
        registry.register(StubProvider("a"))
        registry.register(StubProvider("b"))
        registry.register(StubProvider("c"))
        
        val all = registry.getAll()
        assertEquals(3, all.size)
        assertTrue(all.map { it.id }.containsAll(listOf("a", "b", "c")))
    }

    @Test
    fun `getAll returns empty list when no providers`() {
        assertTrue(registry.getAll().isEmpty())
    }

    // ── Overwrite ────────────────────────────────────────────

    @Test
    fun `registering with same id overwrites`() {
        registry.register(StubProvider("provider", isLocal = false))
        registry.register(StubProvider("provider", isLocal = true))
        
        assertTrue(registry.get("provider").isLocal)
    }
}
