package com.nabd.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Chat
        ) {
            composable(AppRoute.Chat) {
                val chatViewModel: ChatViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return appContainer.provideChatViewModel() as T
                        }
                    }
                )

                NabdChatScreen(
                    viewModel = chatViewModel,
                    onSettingsClick = { navController.navigate(AppRoute.Settings) },
                    onProviderHubClick = { navController.navigate(AppRoute.ProviderHub) },
                    onToolsHubClick = { navController.navigate(AppRoute.ToolsHub) },
                    onTelemetryHubClick = { navController.navigate(AppRoute.TelemetryHub) }
                )
            }

            composable(AppRoute.Autonomy) {
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

            composable(AppRoute.Settings) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(app = application)
                )

                SettingsPage(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoute.ProviderHub) {
                val providerViewModel: ProviderViewModel = viewModel(
                    factory = ProviderViewModel.Factory(app = application)
                )

                ProviderPage(
                    viewModel = providerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoute.ToolsHub) {
                val toolsViewModel: ToolsViewModel = viewModel(
                    factory = ToolsViewModel.Factory(app = application)
                )

                ToolsPage(
                    viewModel = toolsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onWebSearchClick = { navController.navigate(AppRoute.WebSearchConfigHub) }
                )
            }

            composable(AppRoute.TelemetryHub) {
                val telemetryViewModel: TelemetryViewModel = viewModel(
                    factory = TelemetryViewModel.Factory(app = application)
                )

                TelemetryPage(
                    viewModel = telemetryViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AppRoute.GenerationHub) {
                val generationViewModel: GenerationViewModel = viewModel(
                    factory = GenerationViewModel.Factory(app = application)
                )

                GenerationPage(
                    viewModel = generationViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AppRoute.WebSearchConfigHub) {
                val webSearchViewModel: WebSearchConfigViewModel = viewModel(
                    factory = WebSearchConfigViewModel.Factory(app = application)
                )

                WebSearchConfigPage(
                    viewModel = webSearchViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
