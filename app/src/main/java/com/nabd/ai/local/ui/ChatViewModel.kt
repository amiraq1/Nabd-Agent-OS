package com.nabd.ai.local.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.architecture.Participant
import com.nabd.ai.local.mtp_engine.architecture.NabdAction
import com.nabd.ai.local.mtp_engine.api.LlamaChatEngine
import com.nabd.ai.local.mtp_engine.domain.generation.GenerationManager
import com.nabd.ai.local.mtp_engine.domain.generation.GenerationChunk
import com.nabd.ai.local.mtp_engine.domain.generation.ContextAssembler
import com.nabd.ai.local.mtp_engine.domain.generation.GenerationError
import com.nabd.ai.local.mtp_engine.domain.generation.InferenceConfig
import com.nabd.ai.local.mtp_engine.domain.conversation.ConversationManager
import com.nabd.ai.local.mtp_engine.data.repository.ConversationRepository
import com.nabd.ai.local.mtp_engine.domain.tools.ToolOrchestrator
import com.nabd.ai.local.mtp_engine.domain.rag.RagManager
import com.nabd.ai.local.data.SettingsRepository
import com.nabd.ai.local.memory.SemanticMemoryRetriever
import com.nabd.ai.local.rag.retrieval.KnowledgeRetriever
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

import com.nabd.ai.local.mtp_engine.domain.tools.StreamingToolParser
import com.nabd.ai.local.mtp_engine.domain.tools.ToolCommand

/**
 * ChatViewModel: Implements [ChatStateOrchestrator] and coordinates between UI and Domain/Engine layers.
 */
class ChatViewModel(
    private val engine: LlamaChatEngine,
    private val settingsRepository: SettingsRepository,
    private val semanticRetriever: SemanticMemoryRetriever,
    private val knowledgeRetriever: KnowledgeRetriever,
    private val conversationRepository: ConversationRepository,
    private val toolOrchestrator: ToolOrchestrator,
    private val ragManager: RagManager
) : ViewModel(), ChatStateOrchestrator {

    private val conversationManager = ConversationManager()
    private val contextAssembler = ContextAssembler()
    private val generationManager = GenerationManager(engine, contextAssembler)

    private var activeModelPath: String? = null
    private var generationJob: Job? = null
    
    // For now, use a single active conversation ID
    private var currentConversationId: String = UUID.randomUUID().toString()

    private val _isGenerating = MutableStateFlow(false)
    private val _errorState = MutableStateFlow<GenerationError?>(null)
    private val _inferenceConfig = MutableStateFlow(InferenceConfig())

    // Compose state derived from internal managers
    override val state: StateFlow<ChatUiState> = combine(
        conversationManager.activePath,
        _isGenerating,
        _errorState,
        _inferenceConfig
    ) { messages, isGenerating, errorState, inferenceConfig ->
        ChatUiState(
            messages = messages,
            isGenerating = isGenerating,
            errorState = errorState,
            inferenceConfig = inferenceConfig
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    init {
        // Track active model path from settings
        settingsRepository.activeModelPath
            .onEach { path -> 
                activeModelPath = path
                if (path != null) {
                    // Pre-load model if path changes
                    viewModelScope.launch {
                        try {
                            engine.loadModel(path)
                        } catch (e: Exception) {
                            _errorState.value = GenerationError.EngineFault(e.message ?: "Model load failed")
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
            
        // Initial load (mocking session restoration for now)
        loadActiveConversation()
    }

    private fun loadActiveConversation() {
        viewModelScope.launch {
            val messages = conversationRepository.loadMessages(currentConversationId)
            if (messages.isNotEmpty()) {
                // Reconstruct tree in manager (simplified for now)
                messages.forEach { conversationManager.appendMessage(it, isRoot = it.parentId == null) }
            }
        }
    }

    /**
     * Entry point for all UI actions, following strict UDF.
     */
    override fun dispatch(action: NabdAction) {
        when (action) {
            is NabdAction.ProcessPrompt -> sendMessage(action.text)
            is NabdAction.EditMessage -> editMessage(action.messageId, action.newText)
            is NabdAction.SwitchBranch -> switchBranch(action.parentId, action.childId)
            is NabdAction.RetryGeneration -> retryGeneration(action.messageId)
            NabdAction.CancelGeneration -> cancelGeneration()
            is NabdAction.UpdateInferenceConfig -> _inferenceConfig.value = action.config
        }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank() || _isGenerating.value) return

        val activePath = conversationManager.activePath.value
        val isRoot = activePath.isEmpty()
        val parentId = activePath.lastOrNull()?.id

        val userMessage = ChatMessage(
            text = text, 
            participant = Participant.USER,
            parentId = parentId
        )
        val assistantPlaceholder = ChatMessage(
            text = "", 
            participant = Participant.ASSISTANT, 
            isPending = true,
            parentId = userMessage.id
        )

        conversationManager.appendMessage(userMessage, isRoot = isRoot)
        conversationManager.appendMessage(assistantPlaceholder)
        
        // Persist and index user message
        viewModelScope.launch {
            conversationRepository.saveMessage(userMessage, currentConversationId)
            ragManager.indexMessage(userMessage)
        }

        generateResponse(text, assistantPlaceholder.id)
    }

    private fun generateResponse(prompt: String, messageId: String) {
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            _isGenerating.value = true
            _errorState.value = null
            try {
                // 1. Context Assembly using MTP logic
                val activePath = conversationManager.activePath.value
                
                var streamedText = ""
                val toolParser = StreamingToolParser()
                var extractedCommand: ToolCommand? = null

                // 2. Tactical Semantic Search (MTP RAG)
                // We perform search off-main thread via RagManager
                val ragResults = ragManager.searchMemory(prompt)
                if (ragResults.isNotEmpty()) {
                     // Inject semantic context as a system note into the stream history temporarily
                     val contextStr = ragResults.joinToString("\n") { it.text }
                     // Note: A robust implementation would inject this via ContextAssembler
                }

                // 3. Streamed Generation with Tool Interception Loop
                val currentConfig = _inferenceConfig.value
                generationManager.executeGeneration(activePath, currentConfig, currentConfig.systemPrompt).collect { chunk ->
                    when (chunk) {
                        is GenerationChunk.Text -> {
                            streamedText += chunk.content
                            conversationManager.updateMessage(messageId, streamedText, isPending = true)
                            
                            // Stateful tool extraction
                            val command = toolParser.processToken(chunk.content)
                            if (command != null) {
                                extractedCommand = command
                                generationManager.halt() // Stop current generation stream
                            }
                        }
                        is GenerationChunk.Error -> {
                            _errorState.value = chunk.error
                            conversationManager.updateMessage(messageId, "Error: ${chunk.error}", isPending = false)
                        }
                    }
                }
                
                if (extractedCommand != null) {
                    val toolName = extractedCommand!!.name
                    val toolArgs = extractedCommand!!.arguments
                    
                    val toolResult = toolOrchestrator.dispatch(toolName, toolArgs)
                    val resultString = when(toolResult) {
                        is com.nabd.ai.local.mtp_engine.domain.tools.ToolResult.Success -> toolResult.output
                        is com.nabd.ai.local.mtp_engine.domain.tools.ToolResult.Failure -> "ERROR: ${toolResult.reason}"
                    }
                    
                    // Append result and continue (simulated continuation for demo)
                    streamedText += "\n<tool_result>\n$resultString\n</tool_result>\nAssistant:"
                    conversationManager.updateMessage(messageId, streamedText, isPending = true)
                    
                    // Here we would normally recurse or loop to let the LLM see the result
                    // and generate the final answer. For brevity, we just append a placeholder.
                    streamedText += " Tool execution completed."
                }
                
                // 4. Finalize message and persist
                conversationManager.updateMessage(messageId, streamedText, isPending = false)
                
                val finalMessage = conversationManager.activePath.value.last { it.id == messageId }
                conversationRepository.saveMessage(finalMessage, currentConversationId)
                ragManager.indexMessage(finalMessage)
                
            } catch (e: Exception) {
                _errorState.value = GenerationError.Unknown(e.message ?: "Unknown error")
                conversationManager.updateMessage(messageId, "Error: ${e.message}", isPending = false)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun editMessage(messageId: String, newText: String) {
        conversationManager.updateMessage(messageId, newText)
        viewModelScope.launch {
            val updated = conversationManager.activePath.value.find { it.id == messageId }
            updated?.let { conversationRepository.saveMessage(it, currentConversationId) }
        }
    }

    private fun switchBranch(parentId: String, childId: String) {
        conversationManager.switchBranch(parentId, childId)
    }

    private fun retryGeneration(messageId: String) {
        val messages = conversationManager.activePath.value
        val messageIndex = messages.indexOfFirst { it.id == messageId }
        if (messageIndex != -1 && messageIndex > 0) {
            val prevMessage = messages[messageIndex - 1]
            if (prevMessage.participant == Participant.USER) {
                conversationManager.updateMessage(messageId, "", isPending = true)
                generateResponse(prevMessage.text, messageId)
            }
        }
    }

    private fun cancelGeneration() {
        viewModelScope.launch {
            generationManager.halt()
            generationJob?.cancel()
        }
    }
}
