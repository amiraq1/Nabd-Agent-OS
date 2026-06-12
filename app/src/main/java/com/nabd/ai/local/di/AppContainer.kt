package com.nabd.ai.local.di

import android.content.Context
import com.nabd.ai.local.data.SettingsRepository
import com.nabd.ai.local.domain.ModelManager
import com.nabd.ai.local.engine.LlamaEngine
import com.nabd.ai.local.embedding.*
import com.nabd.ai.local.memory.MemoryManager
import com.nabd.ai.local.memory.SemanticMemoryRetriever
import com.nabd.ai.local.memory.db.MemoryDatabase
import com.nabd.ai.local.rag.ingestion.KnowledgeIngestionEngine
import com.nabd.ai.local.rag.ingestion.KnowledgeIngestionManager
import com.nabd.ai.local.rag.retrieval.KnowledgeRetriever
import com.nabd.ai.local.rag.retrieval.HybridRetriever
import com.nabd.ai.local.rag.storage.LocalVectorDatabase
import com.nabd.ai.local.agent.ToolRegistry
import com.nabd.ai.local.agent.orchestrator.ToolOrchestrator
import com.nabd.ai.local.agent.approval.ApprovalManager
import com.nabd.ai.local.workspace.LocalSandboxManager
import com.nabd.ai.local.workspace.WorkspaceObserver
import com.nabd.ai.local.workspace.history.WorkspaceHistoryManager
import com.nabd.ai.local.workspace.search.CodeSearchEngine
import com.nabd.ai.local.intelligence.index.SymbolIndex
import com.nabd.ai.local.intelligence.graph.ProjectGraphManager
import com.nabd.ai.local.intelligence.impact.ImpactAnalyzer
import com.nabd.ai.local.intelligence.memory.ArchitectureMemory
import com.nabd.ai.local.intelligence.refactor.RefactoringPlanner
import com.nabd.ai.local.autonomy.planning.TaskPlanner
import com.nabd.ai.local.autonomy.reflection.ReflectionEngine
import com.nabd.ai.local.autonomy.replanning.ReplanningManager
import com.nabd.ai.local.autonomy.state.PlanStateManager
import com.nabd.ai.local.autonomy.session.SessionManager
import com.nabd.ai.local.autonomy.safety.ExecutionGuardrails
import com.nabd.ai.local.autonomy.history.ExecutionTimeline
import com.nabd.ai.local.autonomy.resources.ResourceMonitor
import com.nabd.ai.local.autonomy.runtime.AutonomousAgentRunner
import java.io.File

class AppContainer(private val context: Context) {

    val mtpContainer by lazy { com.nabd.ai.local.mtp_engine.di.NabdContainer(context) }

    val settingsRepository by lazy { SettingsRepository(context) }
    val modelManager by lazy { ModelManager(context) }
    
    val secureKeyManager by lazy { com.nabd.ai.local.core.SecureKeyManager(context) }
    
    val providerRegistry by lazy { 
        com.nabd.ai.local.engine.DefaultProviderRegistry().apply {
            register(com.nabd.ai.local.engine.LlamaEngine())
            register(com.nabd.ai.local.engine.OpenAIEngine(secureKeyManager))
            register(com.nabd.ai.local.engine.GeminiEngine(secureKeyManager))
            register(com.nabd.ai.local.engine.AnthropicEngine(secureKeyManager))
        }
    }

    val engine by lazy { 
        com.nabd.ai.local.engine.EngineManager(settingsRepository, providerRegistry) 
    }

    val database by lazy { MemoryDatabase.getDatabase(context) }
    
    val embeddingProvider by lazy { 
        OnnxEmbeddingProvider(context, "all-minilm-l6-v2.onnx") 
    }
    
    val vectorStore by lazy { VectorStore(database.memoryEmbeddingDao()) }
    val embeddingManager by lazy { EmbeddingManager(embeddingProvider, vectorStore) }

    val memoryManager by lazy { 
        MemoryManager(database.memoryDao(), embeddingManager) 
    }

    val semanticRetriever by lazy { 
        SemanticMemoryRetriever(database.memoryDao(), vectorStore, embeddingManager) 
    }

    val localVectorDatabase by lazy {
        LocalVectorDatabase(database.knowledgeDao())
    }

    val hybridRetriever by lazy {
        HybridRetriever(localVectorDatabase, embeddingManager)
    }

    val knowledgeRetriever by lazy {
        KnowledgeRetriever(hybridRetriever, database.knowledgeDao())
    }

    val textSplitter by lazy {
        com.nabd.ai.local.rag.chunking.RecursiveCharacterTextSplitter()
    }

    val knowledgeIngestionEngine by lazy {
        KnowledgeIngestionEngine(context, textSplitter, embeddingProvider, localVectorDatabase)
    }

    val knowledgeIngestionManager by lazy {
        KnowledgeIngestionManager(context, database.knowledgeDao(), embeddingManager)
    }

    val conversationRepository by lazy {
        com.nabd.ai.local.mtp_engine.data.repository.ConversationRepository(database.mtpChatDao())
    }

    val workspaceManager by lazy {
        LocalSandboxManager(File(context.filesDir, "workspace"))
    }

    val workspaceHistoryManager by lazy {
        WorkspaceHistoryManager(workspaceManager, File(context.filesDir, "workspace_history"))
    }

    val workspaceObserver by lazy {
        WorkspaceObserver()
    }

    val codeSearchEngine by lazy {
        CodeSearchEngine(workspaceManager)
    }

    val approvalManager by lazy {
        ApprovalManager()
    }

    val symbolIndex by lazy { SymbolIndex() }

    val projectGraphManager by lazy { ProjectGraphManager() }

    val impactAnalyzer by lazy {
        ImpactAnalyzer(symbolIndex, projectGraphManager)
    }

    val architectureMemory by lazy {
        ArchitectureMemory(File(context.filesDir, "architecture.json"))
    }

    val refactoringPlanner by lazy {
        RefactoringPlanner(workspaceManager, codeSearchEngine, symbolIndex)
    }

    val toolRegistry by lazy {
        ToolRegistry().apply {
            register(com.nabd.ai.local.agent.tools.TimeTool())
            register(com.nabd.ai.local.agent.tools.CalculatorTool())
            register(com.nabd.ai.local.agent.tools.MemoryTool(memoryManager, semanticRetriever))
            register(com.nabd.ai.local.agent.tools.KnowledgeTool(knowledgeRetriever, localVectorDatabase))
            register(com.nabd.ai.local.agent.tools.filesystem.ReadFileTool(workspaceManager))
            register(com.nabd.ai.local.agent.tools.filesystem.ListDirectoryTool(workspaceManager))
            register(com.nabd.ai.local.agent.tools.filesystem.WriteFileTool(workspaceManager, workspaceHistoryManager, workspaceObserver))
            register(com.nabd.ai.local.agent.tools.filesystem.EditFileTool(workspaceManager, workspaceHistoryManager, workspaceObserver))
            register(com.nabd.ai.local.agent.tools.filesystem.DeleteFileTool(workspaceManager, workspaceHistoryManager, workspaceObserver))
            register(com.nabd.ai.local.agent.tools.filesystem.SearchFilesTool(codeSearchEngine))
            
            register(com.nabd.ai.local.agent.tools.intelligence.GetFunctionSignatureTool(symbolIndex))
            register(com.nabd.ai.local.agent.tools.intelligence.FindSymbolUsagesTool(codeSearchEngine))
            register(com.nabd.ai.local.agent.tools.intelligence.FindDefinitionTool(symbolIndex))
            register(com.nabd.ai.local.agent.tools.intelligence.ImpactAnalysisTool(impactAnalyzer))
            register(com.nabd.ai.local.agent.tools.intelligence.SummarizeArchitectureTool(architectureMemory))
            register(com.nabd.ai.local.agent.tools.intelligence.CrossFileRefactorTool(refactoringPlanner, workspaceManager, workspaceHistoryManager, workspaceObserver))
        }
    }

    val taskPlanner by lazy { TaskPlanner(engine) }
    val reflectionEngine by lazy { ReflectionEngine(engine) }
    val replanningManager by lazy { ReplanningManager() }
    
    val planStateManager by lazy { PlanStateManager(File(context.filesDir, "state")) }
    val sessionManager by lazy { SessionManager(planStateManager) }
    
    val executionGuardrails by lazy { ExecutionGuardrails() }
    val executionTimeline by lazy { ExecutionTimeline() }
    val resourceMonitor by lazy { ResourceMonitor(context) }

    val sandbox by lazy { com.nabd.ai.local.autonomy.safety.ToolSandbox(workspaceManager.workspaceRoot) }

    val toolOrchestrator by lazy {
        ToolOrchestrator(engine, toolRegistry, approvalManager, sandbox)
    }

    val knowledgeMemory by lazy { com.nabd.ai.local.autonomy.memory.KnowledgeMemory() }
    
    val multiAgentCoordinator by lazy {
        com.nabd.ai.local.autonomy.coordination.MultiAgentCoordinator().apply {
            registerAgent(com.nabd.ai.local.autonomy.coordination.PlannerAgent(planner = taskPlanner))
            registerAgent(com.nabd.ai.local.autonomy.coordination.ExecutorAgent(orchestrator = toolOrchestrator))
            registerAgent(com.nabd.ai.local.autonomy.coordination.ReviewerAgent(reflectionEngine = reflectionEngine))
            registerAgent(com.nabd.ai.local.autonomy.coordination.ReflectionAgent(replanningManager = replanningManager))
        }
    }

    val autonomousAgentRunner by lazy {
        AutonomousAgentRunner(
            multiAgentCoordinator, replanningManager, sessionManager, executionGuardrails, executionTimeline, resourceMonitor
        )
    }

    fun provideChatViewModel(): com.nabd.ai.local.ui.ChatViewModel {
        return com.nabd.ai.local.ui.ChatViewModel(
            mtpContainer.llamaEngine,
            settingsRepository,
            semanticRetriever,
            knowledgeRetriever,
            mtpContainer.conversationRepository,
            mtpContainer.toolOrchestrator,
            mtpContainer.ragManager
        )
    }

    fun provideSettingsViewModel(): com.nabd.ai.local.ui.SettingsViewModel {
        return com.nabd.ai.local.ui.SettingsViewModel(modelManager, settingsRepository, secureKeyManager, knowledgeIngestionManager, database)
    }
}
