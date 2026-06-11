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
import com.nabd.ai.local.ui.ChatScreen
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
                return ChatViewModel(
                    appContainer.engine,
                    appContainer.settingsRepository,
                    appContainer.semanticRetriever,
                    appContainer.knowledgeRetriever
                ) as T
            }
        }
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(
                    appContainer.modelManager,
                    appContainer.settingsRepository,
                    appContainer.knowledgeIngestionManager,
                    appContainer.database
                ) as T
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
                    val uiState by chatViewModel.uiState.collectAsState()
                    ChatScreen(uiState, chatViewModel::onIntent)
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
                        onGeminiKeyChange = settingsViewModel::setGeminiApiKey
                    )
                }
            }
        }

        // Minimal floating navigation
        FloatingMinimalNav(
            currentRoute = activeRoute,
            onRouteSelect = { activeRoute = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
fun FloatingMinimalNav(
    currentRoute: AppRoute,
    onRouteSelect: (AppRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavDot(
            isActive = currentRoute == AppRoute.Chat,
            onClick = { onRouteSelect(AppRoute.Chat) }
        )
        NavDot(
            isActive = currentRoute == AppRoute.Autonomy,
            onClick = { onRouteSelect(AppRoute.Autonomy) }
        )
        NavDot(
            isActive = currentRoute == AppRoute.Settings,
            onClick = { onRouteSelect(AppRoute.Settings) }
        )
    }
}

@Composable
fun NavDot(isActive: Boolean, onClick: () -> Unit) {
    val dotColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
    val dotSize = if (isActive) 8.dp else 6.dp

    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .wrapContentSize(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}
