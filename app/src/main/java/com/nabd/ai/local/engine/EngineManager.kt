package com.nabd.ai.local.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nabd.ai.local.data.SettingsRepository

class EngineManager(
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : LlmProvider {

    private var activeEngine: LlmProvider = LlamaEngine()

    private val _state = MutableStateFlow<EngineState>(EngineState.Unloaded)
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    init {
        coroutineScope.launch {
            combine(
                settingsRepository.activeProvider,
                settingsRepository.openAiApiKey,
                settingsRepository.geminiApiKey
            ) { providerName, openAiKey, geminiKey ->
                Triple(providerName, openAiKey, geminiKey)
            }.collect { (providerName, openAiKey, geminiKey) ->
                
                // Unload previous
                activeEngine.unloadModel()

                activeEngine = when (providerName) {
                    ProviderType.OPENAI.name -> OpenAIEngine(openAiKey ?: "")
                    ProviderType.GEMINI.name -> GeminiEngine(geminiKey ?: "")
                    else -> LlamaEngine()
                }

                // Forward status
                launch {
                    activeEngine.state.collect { 
                        _state.value = it 
                    }
                }
            }
        }
    }

    override suspend fun loadModel(modelPath: String) {
        activeEngine.loadModel(modelPath)
    }

    override fun generateText(prompt: String, grammar: String?): Flow<String> {
        return activeEngine.generateText(prompt, grammar)
    }

    override fun stopGeneration() {
        activeEngine.stopGeneration()
    }

    override fun unloadModel() {
        activeEngine.unloadModel()
    }
}
