package com.nabd.ai.local.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nabd.ai.local.engine.EngineState
import com.nabd.ai.local.engine.LlmProvider
import com.nabd.ai.local.data.SettingsRepository
import com.nabd.ai.local.memory.SemanticMemoryRetriever
import com.nabd.ai.local.rag.retrieval.KnowledgeRetriever
import com.nabd.ai.local.prompt.PromptAssembler
import com.nabd.ai.local.prompt.PromptContext
import com.nabd.ai.local.prompt.ContextWindowStrategy
import com.nabd.ai.local.prompt.templates.ChatTemplateManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val provider: LlmProvider,
    private val settingsRepository: SettingsRepository,
    private val semanticRetriever: SemanticMemoryRetriever,
    private val knowledgeRetriever: KnowledgeRetriever
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null
    private var activeModelPath: String? = null
    
    private val promptAssembler = PromptAssembler(ContextWindowStrategy(), ChatTemplateManager())

    init {
        // Observe active model path
        settingsRepository.activeModelPath
            .onEach { path -> activeModelPath = path }
            .launchIn(viewModelScope)

        // Observe engine state changes
        provider.state
            .onEach { engineState ->
                _uiState.update { 
                    it.copy(
                        engineState = engineState,
                        isGenerating = engineState is EngineState.Generating,
                        isModelLoaded = engineState is EngineState.Loaded || engineState is EngineState.Generating,
                        error = if (engineState is EngineState.Failed) engineState.error else it.error
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: ChatIntent) {
        when (intent) {
            ChatIntent.LoadModel -> loadModel()
            ChatIntent.UnloadModel -> unloadModel()
            ChatIntent.StopGeneration -> stopGeneration()
            ChatIntent.SendPrompt -> sendPrompt()
            is ChatIntent.UpdatePrompt -> updatePrompt(intent.prompt)
            ChatIntent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadModel() {
        val path = activeModelPath
        if (path == null) {
            _uiState.update { it.copy(error = "No model selected. Please import and select a model in settings.") }
            return
        }

        viewModelScope.launch {
            try {
                provider.loadModel(path)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun unloadModel() {
        provider.unloadModel()
    }

    private fun stopGeneration() {
        provider.stopGeneration()
        generationJob?.cancel()
    }

    private fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(currentPrompt = prompt) }
    }

    private fun sendPrompt() {
        val prompt = _uiState.value.currentPrompt
        if (prompt.isBlank() || _uiState.value.isGenerating) return

        val userMessage = ChatMessage(text = prompt, isUser = true)
        val assistantPlaceholder = ChatMessage(text = "", isUser = false, isPending = true)

        val historyStrings = _uiState.value.messages.map { 
            if (it.isUser) "User: ${it.text}" else "Assistant: ${it.text}" 
        }

        _uiState.update { 
            it.copy(
                messages = it.messages + userMessage + assistantPlaceholder,
                currentPrompt = ""
            )
        }

        generationJob = viewModelScope.launch {
            try {
                // 1. Retrieve relevant memories
                val contextualMemories = semanticRetriever.retrieveSemanticContext(prompt)
                
                // 2. Retrieve relevant document knowledge
                val knowledgeBase = knowledgeRetriever.retrieveKnowledge(prompt)
                
                // 3. Assemble full context
                val promptContext = PromptContext(
                    systemPrompt = "You are Nabd, a highly capable AI assistant.",
                    toolDefinitions = "", // Tools would go here if orchestrated from ChatViewModel
                    memoryContext = contextualMemories,
                    knowledgeContext = knowledgeBase,
                    conversationHistory = historyStrings,
                    userInput = prompt
                )

                val assembledPrompt = promptAssembler.assemble(promptContext)

                var streamedText = ""
                provider.generateText(assembledPrompt).collect { token ->
                    streamedText += token
                    updateLastAssistantMessage(streamedText, isPending = true)
                }
                updateLastAssistantMessage(streamedText, isPending = false)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                updateLastAssistantMessage("Error: ${e.message}", isPending = false)
            }
        }
    }

    private fun updateLastAssistantMessage(text: String, isPending: Boolean) {
        _uiState.update { state ->
            val updatedMessages = state.messages.toMutableList()
            val lastIndex = updatedMessages.indexOfLast { !it.isUser }
            if (lastIndex != -1) {
                updatedMessages[lastIndex] = updatedMessages[lastIndex].copy(
                    text = text,
                    isPending = isPending
                )
            }
            state.copy(messages = updatedMessages)
        }
    }
}
