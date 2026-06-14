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
import com.nabd.ai.agora.ui.settings.SettingsPage
import com.nabd.ai.agora.ui.settings.SettingsViewModel
import androidx.compose.ui.platform.LocalContext
import android.app.Application
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
        factory = SettingsViewModel.Factory(
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
                is AppRoute.Chat -> {
                    NabdChatScreen(
                        viewModel = chatViewModel,
                        onSettingsClick = { activeRoute = AppRoute.Settings }
                    )
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
                    SettingsPage(
                        viewModel = settingsViewModel,
                        onBack = { activeRoute = AppRoute.Chat }
                    )
                }
            }
        }
    }
}
