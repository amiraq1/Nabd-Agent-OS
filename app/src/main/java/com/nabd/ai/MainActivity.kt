package com.nabd.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nabd.ai.local.data.SettingsRepository
import com.nabd.ai.local.domain.ModelManager
import com.nabd.ai.local.engine.LlamaEngine
import com.nabd.ai.local.ui.*
import com.nabd.ai.ui.theme.NabdTheme
import com.nabd.ai.local.embedding.*
import com.nabd.ai.local.memory.MemoryManager
import com.nabd.ai.local.memory.SemanticMemoryRetriever
import com.nabd.ai.local.memory.db.MemoryDatabase

import com.nabd.ai.local.rag.db.KnowledgeDao
import com.nabd.ai.local.rag.ingestion.KnowledgeIngestionManager
import com.nabd.ai.local.rag.retrieval.KnowledgeRetriever
import com.nabd.ai.local.agent.tools.KnowledgeTool
import com.nabd.ai.local.agent.ToolRegistry
import com.nabd.ai.local.agent.tools.MemoryTool
import com.nabd.ai.local.agent.tools.TimeTool
import com.nabd.ai.local.agent.tools.CalculatorTool

class MainActivity : ComponentActivity() {

    private val settingsRepository by lazy { SettingsRepository(applicationContext) }
    private val modelManager by lazy { ModelManager(applicationContext) }
    private val engine by lazy { LlamaEngine() }

    private val database by lazy { MemoryDatabase.getDatabase(applicationContext) }
    private val embeddingProvider by lazy { 
        OnnxEmbeddingProvider(applicationContext, "/data/data/com.nabd.ai/files/models/all-minilm-l6-v2.onnx") 
    }
    private val vectorStore by lazy { VectorStore(database.memoryEmbeddingDao()) }
    private val embeddingManager by lazy { EmbeddingManager(embeddingProvider, vectorStore) }

    private val memoryManager by lazy { 
        MemoryManager(database.memoryDao(), embeddingManager) 
    }

    private val semanticRetriever by lazy { 
        SemanticMemoryRetriever(database.memoryDao(), vectorStore, embeddingManager) 
    }

    private val knowledgeRetriever by lazy {
        val hybridRetriever = com.nabd.ai.local.rag.retrieval.HybridRetriever(localVectorDatabase, embeddingManager)
        com.nabd.ai.local.rag.retrieval.KnowledgeRetriever(hybridRetriever, database.knowledgeDao())
    }

    private val localVectorDatabase by lazy {
        com.nabd.ai.local.rag.storage.LocalVectorDatabase(database.knowledgeDao())
    }

    private val textSplitter by lazy {
        com.nabd.ai.local.rag.chunking.RecursiveCharacterTextSplitter()
    }

    private val knowledgeIngestionEngine by lazy {
        com.nabd.ai.local.rag.ingestion.KnowledgeIngestionEngine(applicationContext, textSplitter, embeddingProvider, localVectorDatabase)
    }

    private val knowledgeIngestionManager by lazy {
        com.nabd.ai.local.rag.ingestion.KnowledgeIngestionManager(applicationContext, database.knowledgeDao(), embeddingManager)
    }

    private val workspaceManager by lazy {
        com.nabd.ai.local.workspace.LocalSandboxManager(java.io.File(applicationContext.filesDir, "workspace"))
    }

    private val workspaceHistoryManager by lazy {
        com.nabd.ai.local.workspace.history.WorkspaceHistoryManager(workspaceManager, java.io.File(applicationContext.filesDir, "workspace_history"))
    }

    private val workspaceObserver by lazy {
        com.nabd.ai.local.workspace.WorkspaceObserver()
    }

    private val codeSearchEngine by lazy {
        com.nabd.ai.local.workspace.search.CodeSearchEngine(workspaceManager)
    }

    private val approvalManager by lazy {
        com.nabd.ai.local.agent.approval.ApprovalManager()
    }

    private val symbolIndex by lazy { com.nabd.ai.local.intelligence.index.SymbolIndex() }

    private val projectGraphManager by lazy { com.nabd.ai.local.intelligence.graph.ProjectGraphManager() }

    private val impactAnalyzer by lazy {
        com.nabd.ai.local.intelligence.impact.ImpactAnalyzer(symbolIndex, projectGraphManager)
    }

    private val architectureMemory by lazy {
        com.nabd.ai.local.intelligence.memory.ArchitectureMemory(java.io.File(applicationContext.filesDir, "architecture.json"))
    }

    private val refactoringPlanner by lazy {
        com.nabd.ai.local.intelligence.refactor.RefactoringPlanner(workspaceManager, codeSearchEngine, symbolIndex)
    }

    private val toolRegistry by lazy {
        com.nabd.ai.local.agent.ToolRegistry().apply {
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
            
            // Intelligence Tools
            register(com.nabd.ai.local.agent.tools.intelligence.GetFunctionSignatureTool(symbolIndex))
            register(com.nabd.ai.local.agent.tools.intelligence.FindSymbolUsagesTool(codeSearchEngine))
            register(com.nabd.ai.local.agent.tools.intelligence.FindDefinitionTool(symbolIndex))
            register(com.nabd.ai.local.agent.tools.intelligence.ImpactAnalysisTool(impactAnalyzer))
            register(com.nabd.ai.local.agent.tools.intelligence.SummarizeArchitectureTool(architectureMemory))
            register(com.nabd.ai.local.agent.tools.intelligence.CrossFileRefactorTool(refactoringPlanner, workspaceManager, workspaceHistoryManager, workspaceObserver))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Execute Identity Migration Layers safely before any Room DB or File accesses
        com.nabd.ai.local.core.migration.DatabaseMigrationManager(applicationContext).migrate()
        com.nabd.ai.local.core.migration.WorkspaceMigrationManager(applicationContext).migrate()
        com.nabd.ai.local.core.migration.ModelMigrationManager(applicationContext).migrate()
        com.nabd.ai.local.core.migration.SettingsMigrationManager(applicationContext).migrate()

        setContent {
            NabdTheme {
                MainApp(
                    engine, 
                    settingsRepository, 
                    modelManager, 
                    memoryManager, 
                    semanticRetriever,
                    knowledgeRetriever,
                    knowledgeIngestionManager,
                    database
                )
            }
        }
    }
}

@Composable
fun MainApp(
    engine: LlamaEngine,
    settingsRepository: SettingsRepository,
    modelManager: ModelManager,
    memoryManager: MemoryManager,
    semanticRetriever: SemanticMemoryRetriever,
    knowledgeRetriever: KnowledgeRetriever,
    knowledgeIngestionManager: KnowledgeIngestionManager,
    database: MemoryDatabase
) {
    var currentScreen by remember { mutableStateOf("chat") }

    val chatViewModel = remember { ChatViewModel(engine, settingsRepository, semanticRetriever, knowledgeRetriever) }
    val settingsViewModel = remember { SettingsViewModel(modelManager, settingsRepository, knowledgeIngestionManager, database) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == "chat",
                    onClick = { currentScreen = "chat" },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Chat") },
                    label = { Text("Chat") }
                )
                NavigationBarItem(
                    selected = currentScreen == "settings",
                    onClick = { currentScreen = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (currentScreen == "chat") {
                ChatScreen(chatViewModel)
            } else {
                SettingsScreen(settingsViewModel)
            }
        }
    }
}
