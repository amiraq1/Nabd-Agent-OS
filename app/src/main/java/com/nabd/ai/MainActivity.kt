package com.nabd.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.nabd.ai.local.data.SettingsRepository
import com.nabd.ai.local.domain.ModelManager
import com.nabd.ai.local.engine.LlamaEngine
import com.nabd.ai.local.ui.*
import com.nabd.ai.ui.theme.NabdTheme
import com.nabd.ai.local.memory.MemoryManager
import com.nabd.ai.local.memory.SemanticMemoryRetriever
import com.nabd.ai.local.memory.db.MemoryDatabase
import com.nabd.ai.local.rag.ingestion.KnowledgeIngestionManager
import com.nabd.ai.local.rag.retrieval.KnowledgeRetriever
import com.nabd.ai.local.autonomy.runtime.AutonomousAgentRunner
import com.nabd.ai.local.autonomy.history.ExecutionTimeline
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val container = (application as NabdApplication).container

        // Execute Identity Migration Layers safely before any Room DB or File accesses
        com.nabd.ai.local.core.migration.DatabaseMigrationManager(applicationContext).migrate()
        com.nabd.ai.local.core.migration.WorkspaceMigrationManager(applicationContext).migrate()
        com.nabd.ai.local.core.migration.ModelMigrationManager(applicationContext).migrate()
        com.nabd.ai.local.core.migration.SettingsMigrationManager(applicationContext).migrate()

        setContent {
            NabdTheme {
                MainApp(
                    engine = container.engine,
                    settingsRepository = container.settingsRepository,
                    modelManager = container.modelManager,
                    memoryManager = container.memoryManager,
                    semanticRetriever = container.semanticRetriever,
                    knowledgeRetriever = container.knowledgeRetriever,
                    knowledgeIngestionManager = container.knowledgeIngestionManager,
                    database = container.database,
                    agentRunner = container.autonomousAgentRunner,
                    timeline = container.executionTimeline
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
    database: MemoryDatabase,
    agentRunner: AutonomousAgentRunner,
    timeline: ExecutionTimeline
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
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("Chat") }
                )
                NavigationBarItem(
                    selected = currentScreen == "autonomy",
                    onClick = { currentScreen = "autonomy" },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Autonomy") },
                    label = { Text("Autonomy") }
                )
                NavigationBarItem(
                    selected = currentScreen == "settings",
                    onClick = { currentScreen = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                "chat" -> {
                    val uiState by chatViewModel.uiState.collectAsState()
                    ChatScreen(uiState = uiState, onIntent = chatViewModel::onIntent)
                }
                "autonomy" -> {
                    val agentState by agentRunner.state.collectAsState()
                    val currentPlan by agentRunner.currentPlan.collectAsState()
                    val scope = rememberCoroutineScope()
                    
                    com.nabd.ai.local.ui.autonomy.AgentExecutionScreen(
                        state = agentState,
                        plan = currentPlan, 
                        timeline = timeline.events,
                        onPause = { agentRunner.pause() },
                        onResume = { 
                            scope.launch {
                                agentRunner.resumeGoal()
                            }
                        },
                        onCancel = { agentRunner.cancel() }
                    )
                }
                "settings" -> {
                    val uiState by settingsViewModel.uiState.collectAsState()
                    SettingsScreen(
                        uiState = uiState,
                        onImport = settingsViewModel::importModel,
                        onSelect = settingsViewModel::selectModel,
                        onDelete = settingsViewModel::deleteModel,
                        onImportDocument = settingsViewModel::importDocument,
                        onDeleteDocument = settingsViewModel::deleteDocument
                    )
                }
            }
        }
    }
}
