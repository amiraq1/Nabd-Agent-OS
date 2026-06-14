package com.nabd.ai.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nabd.ai.local.di.AppContainer
import com.nabd.ai.local.mtp_engine.ui.chat.NabdChatScreen
import com.nabd.ai.local.ui.ChatViewModel
import com.nabd.ai.agora.ui.settings.SettingsPage
import com.nabd.ai.agora.ui.settings.SettingsViewModel
import com.nabd.ai.agora.ui.provider.ProviderPage
import com.nabd.ai.agora.ui.provider.ProviderViewModel
import com.nabd.ai.agora.ui.tools.ToolsPage
import com.nabd.ai.agora.ui.tools.ToolsViewModel
import com.nabd.ai.agora.ui.telemetry.TelemetryPage
import com.nabd.ai.agora.ui.telemetry.TelemetryViewModel
import com.nabd.ai.agora.ui.generation.GenerationPage
import com.nabd.ai.agora.ui.generation.GenerationViewModel
import com.nabd.ai.agora.ui.search.WebSearchConfigPage
import com.nabd.ai.agora.ui.search.WebSearchConfigViewModel
import com.nabd.ai.agora.navigation.AppRoute
import android.app.Application
import kotlinx.coroutines.launch

@Composable
fun AvantGardeAppLayout(appContainer: AppContainer) {
    var activeRoute by remember { mutableStateOf(AppRoute.Chat) }
    
    val dummyNavController = object : androidx.navigation.NavController(LocalContext.current) {
        override fun popBackStack(): Boolean {
            activeRoute = AppRoute.Chat
            return true
        }
    }

    val chatViewModel: ChatViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return appContainer.provideChatViewModel() as T
            }
        }
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            app = LocalContext.current.applicationContext as Application
        )
    )

    val providerViewModel: ProviderViewModel = viewModel(
        factory = ProviderViewModel.Factory(
            app = LocalContext.current.applicationContext as Application
        )
    )

    val toolsViewModel: ToolsViewModel = viewModel(
        factory = ToolsViewModel.Factory(
            app = LocalContext.current.applicationContext as Application
        )
    )

    val telemetryViewModel: TelemetryViewModel = viewModel(
        factory = TelemetryViewModel.Factory(
            app = LocalContext.current.applicationContext as Application
        )
    )

    val generationViewModel: GenerationViewModel = viewModel(
        factory = GenerationViewModel.Factory(
            app = LocalContext.current.applicationContext as Application
        )
    )

    val webSearchViewModel: WebSearchConfigViewModel = viewModel(
        factory = WebSearchConfigViewModel.Factory(
            app = LocalContext.current.applicationContext as Application
        )
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
                AppRoute.Chat -> {
                    NabdChatScreen(
                        viewModel = chatViewModel,
                        onSettingsClick = { activeRoute = AppRoute.Settings },
                        onProviderHubClick = { activeRoute = AppRoute.ProviderHub },
                        onToolsHubClick = { activeRoute = AppRoute.ToolsHub },
                        onTelemetryHubClick = { activeRoute = AppRoute.TelemetryHub }
                    )
                }
                AppRoute.Autonomy -> {
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
                AppRoute.Settings -> {
                    SettingsPage(
                        viewModel = settingsViewModel,
                        onBack = { activeRoute = AppRoute.Chat }
                    )
                }
                AppRoute.ProviderHub -> {
                    ProviderPage(
                        viewModel = providerViewModel,
                        onBack = { activeRoute = AppRoute.Chat }
                    )
                }
                AppRoute.ToolsHub -> {
                    ToolsPage(
                        navController = dummyNavController,
                        viewModel = toolsViewModel
                    )
                }
                AppRoute.TelemetryHub -> {
                    TelemetryPage(
                        navController = dummyNavController,
                        viewModel = telemetryViewModel
                    )
                }
                AppRoute.GenerationHub -> {
                    GenerationPage(
                        navController = dummyNavController,
                        viewModel = generationViewModel
                    )
                }
                AppRoute.WebSearchConfigHub -> {
                    WebSearchConfigPage(
                        navController = dummyNavController,
                        viewModel = webSearchViewModel
                    )
                }
            }
        }
    }
}
