package com.nabd.ai.local.mtp_engine.di

import android.content.Context
import com.nabd.ai.local.mtp_engine.api.LlamaChatEngine
import com.nabd.ai.local.memory.db.MemoryDatabase
import com.nabd.ai.local.mtp_engine.data.repository.ConversationRepository
import com.nabd.ai.local.mtp_engine.domain.conversation.ConversationManager
import com.nabd.ai.local.mtp_engine.domain.generation.ContextAssembler
import com.nabd.ai.local.mtp_engine.domain.generation.GenerationManager
import com.nabd.ai.local.mtp_engine.domain.tools.MemoryFileSystemTool
import com.nabd.ai.local.mtp_engine.domain.tools.RagToolProvider
import com.nabd.ai.local.mtp_engine.domain.tools.ShellToolProvider
import com.nabd.ai.local.mtp_engine.domain.tools.ToolOrchestrator
import com.nabd.ai.local.mtp_engine.domain.rag.RagManager
import com.nabd.ai.local.engine.LlamaEngine
import com.nabd.ai.local.ui.ChatStateOrchestrator
import com.nabd.ai.local.ui.ChatViewModel
import com.nabd.ai.local.data.SettingsRepository
import com.nabd.ai.local.embedding.*
import com.nabd.ai.local.memory.SemanticMemoryRetriever
import com.nabd.ai.local.rag.retrieval.KnowledgeRetriever
import com.nabd.ai.local.rag.retrieval.HybridRetriever
import com.nabd.ai.local.rag.storage.LocalVectorDatabase
import java.io.File

/**
 * NabdContainer: Dependency Injection for the MTP Engine.
 * Intentional Minimalism - Pure Kotlin Lazy DI, Zero Reflection/Annotation Overhead.
 */
class NabdContainer(private val applicationContext: Context) {

    // 1. Data Layer (Persistence)
    private val database by lazy { MemoryDatabase.getDatabase(applicationContext) }
    val conversationRepository by lazy { ConversationRepository(database.mtpChatDao()) }
    val settingsRepository by lazy { SettingsRepository(applicationContext) }

    // 2. Domain Layer (State & Context)
    val conversationManager by lazy { ConversationManager() }
    val contextAssembler by lazy { ContextAssembler(maxContextTokens = 4096) }
    
    // 3. API / Native Engine Layer
    val llamaEngine by lazy { LlamaChatEngine.getInstance() }
    
    // RAG / Semantic Stack
    val ragManager by lazy { RagManager(llamaEngine as LlamaEngine, conversationRepository) }

    // 4. Tools Layer
    private val workspaceDir by lazy { File(applicationContext.filesDir, "workspace").apply { mkdirs() } }
    val memoryFileSystemTool by lazy { MemoryFileSystemTool(workspaceDir) }
    val ragToolProvider by lazy { RagToolProvider(ragManager) }
    val shellToolProvider by lazy { ShellToolProvider(workspaceDir) }
    val toolOrchestrator by lazy { 
        ToolOrchestrator(
            providers = listOf(memoryFileSystemTool, ragToolProvider, shellToolProvider)
        ) 
    }

    // 5. Orchestration Layer (Generation & State)
    val generationManager by lazy { 
        GenerationManager(
            llamaEngine = llamaEngine, 
            contextAssembler = contextAssembler
        ) 
    }
}
