package com.nabd.ai.local.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.nabd.ai.local.data.SettingsRepository

/**
 * EngineManager: Orchestrates multiple LLM providers and manages their lifecycle.
 */
class EngineManager(
    private val settingsRepository: SettingsRepository,
    private val providerRegistry: ProviderRegistry,
    private val metrics: ProviderMetrics = ProviderMetrics(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : LlmProvider {

    override val id: String = "engine_manager"
    override val name: String = "Engine Manager"
    override val isLocal: Boolean get() = activeEngine?.isLocal ?: true

    private var activeEngine: LlmProvider? = null

    private val _state = MutableStateFlow<EngineState>(EngineState.Uninitialized)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    init {
        coroutineScope.launch {
            combine(
                settingsRepository.activeProvider,
                settingsRepository.activeModelPath
            ) { providerName, modelPath ->
                providerName to modelPath
            }.collect { (providerName, modelPath) ->
                switchProvider(providerName, modelPath)
            }
        }
    }

    private suspend fun switchProvider(providerName: String, modelPath: String?) {
        // Shutdown previous
        activeEngine?.shutdown()

        val providerId = when (providerName) {
            ProviderType.OPENAI.name -> "openai"
            ProviderType.GEMINI.name -> "gemini"
            ProviderType.ANTHROPIC.name -> "anthropic"
            else -> "local_llama"
        }

        try {
            val engine = providerRegistry.get(providerId)
            activeEngine = engine

            val config = if (engine.isLocal) {
                ProviderConfig.Local(providerId, "default", modelPath ?: "")
            } else {
                // For cloud providers, we might want to get the specific modelId from settings too.
                // For now, using a default.
                val cloudModelId = when (providerId) {
                    "openai" -> "gpt-4o-mini"
                    "gemini" -> "gemini-1.5-flash"
                    "anthropic" -> "claude-3-5-sonnet-20240620"
                    else -> "default"
                }
                ProviderConfig.Cloud(providerId, cloudModelId)
            }

            engine.initialize(config)

            // Forward status
            coroutineScope.launch {
                engine.state.collect { 
                    _state.value = it 
                }
            }
        } catch (e: Exception) {
            _state.value = EngineState.Error(e)
        }
    }

    override suspend fun initialize(config: ProviderConfig) {
        activeEngine?.initialize(config)
    }

    override suspend fun shutdown() {
        activeEngine?.shutdown()
        _state.value = EngineState.Released
    }

    override suspend fun generateText(request: GenerationRequest): Flow<GenerationChunk> {
        val currentEngine = activeEngine ?: return flowOf()
        val startTime = System.currentTimeMillis()
        
        return currentEngine.generateText(request)
            .onCompletion { cause ->
                val duration = System.currentTimeMillis() - startTime
                if (cause == null) {
                    metrics.recordSuccess(currentEngine.id, request.modelId, duration, 0)
                } else {
                    metrics.recordFailure(currentEngine.id, request.modelId, cause)
                }
            }
    }
}
