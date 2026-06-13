package com.nabd.ai.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nabd.ai.local.di.AppContainer
import com.nabd.ai.local.mtp_engine.ui.chat.NabdChatScreen
import com.nabd.ai.local.ui.ChatViewModel
import com.nabd.ai.local.ui.SettingsScreen
import com.nabd.ai.local.ui.SettingsViewModel
import kotlinx.coroutines.launch

sealed class AppRoute {
    object Chat : AppRoute()
    object Autonomy : AppRoute()
    object Settings : AppRoute()
}

@Composable
fun AvantGardeAppLayout(appContainer: AppContainer) {
    var activeRoute by remember { mutableStateOf<AppRoute>(AppRoute.Chat) }

    val chatViewModel: ChatViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return appContainer.provideChatViewModel() as T
            }
        }
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return appContainer.provideSettingsViewModel() as T
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Crossfade(
            targetState = activeRoute,
            animationSpec = tween(durationMillis = 400),
            label = "RouteTransition"
        ) { route ->
            when (route) {
                is AppRoute.Chat -> {
                    NabdChatScreen(chatViewModel)
                }
                is AppRoute.Autonomy -> {
                    val agentRunner = appContainer.autonomousAgentRunner
                    val timeline = appContainer.executionTimeline
                    
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
                is AppRoute.Settings -> {
                    val uiState by settingsViewModel.uiState.collectAsState()
                    SettingsScreen(
                        uiState = uiState,
                        onImport = settingsViewModel::importModel,
                        onSelect = settingsViewModel::selectModel,
                        onDelete = settingsViewModel::deleteModel,
                        onImportDocument = settingsViewModel::importDocument,
                        onDeleteDocument = settingsViewModel::deleteDocument,
                        onProviderSelect = settingsViewModel::setProvider,
                        onOpenAiKeyChange = settingsViewModel::setOpenAiApiKey,
                        onGeminiKeyChange = settingsViewModel::setGeminiApiKey,
                        onAnthropicKeyChange = settingsViewModel::setAnthropicApiKey
                    )
                }
            }
        }
    }
}
