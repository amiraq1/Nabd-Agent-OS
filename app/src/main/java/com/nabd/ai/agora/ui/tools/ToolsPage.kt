package com.nabd.ai.agora.ui.tools

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nabd.ai.agora.navigation.AppRoute
import kotlinx.coroutines.flow.StateFlow

private val BG_CARD        = Color(0xFF161A23)
private val BORDER_SUBTLE  = Color(0xFF8A94A6).copy(alpha = 0.15f)
private val ACCENT_NEON    = Color(0xFF00FF66)
private val TEXT_MUTED     = Color(0xFF5B6B85)
private val CORNER_CARD    = RoundedCornerShape(14.dp)
private val FONT_MONO      = FontFamily.Monospace

private data class ToolEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isEnabled: StateFlow<Boolean>,
    val onToggle: (Boolean) -> Unit,
    val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ToolsPage(
    navController: NavController,
    viewModel: ToolsViewModel,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isShellEnabled by viewModel.isShellExecutionEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(isShellEnabled) {
        if (isShellEnabled) {
            snackbarHostState.showSnackbar(
                message = "Shell execution grants elevated access. Use with caution.",
                duration = SnackbarDuration.Short
            )
        }
    }

    val entries = listOf(
        ToolEntry(
            title    = "Web Search",
            subtitle = "Configure web search for all providers",
            icon     = Icons.Default.Public,
            isEnabled = viewModel.isWebSearchEnabled,
            onToggle  = viewModel::toggleWebSearch,
            onClick   = { navController.navigate(AppRoute.WebSearchConfigHub) }
        ),
        ToolEntry(
            title    = "Conversation Search",
            subtitle = "Access conversation history and search method",
            icon     = Icons.Default.Search,
            isEnabled = viewModel.isConversationSearchEnabled,
            onToggle  = viewModel::toggleConversationSearch
        ),
        ToolEntry(
            title    = "Shell",
            subtitle = "Remote shell command execution",
            icon     = Icons.Default.Code,
            isEnabled = viewModel.isShellExecutionEnabled,
            onToggle  = viewModel::toggleShellExecution
        )
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Tools",
                            fontFamily = FONT_MONO,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1117))
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFF0B0E14)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "// BUILT-IN TOOLS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TEXT_MUTED,
                        fontFamily = FONT_MONO,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(entries) { entry ->
                    ToolBentoRow(entry = entry)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ToolBentoRow(entry: ToolEntry) {
    val isEnabled by entry.isEnabled.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BORDER_SUBTLE, CORNER_CARD)
            .background(BG_CARD, CORNER_CARD)
            .clickable(onClick = entry.onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = entry.icon,
            contentDescription = null,
            tint = TEXT_MUTED,
            modifier = Modifier.size(22.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = entry.title,
                fontFamily = FONT_MONO,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.subtitle,
                fontFamily = FONT_MONO,
                color = TEXT_MUTED,
                fontSize = 11.sp
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = isEnabled,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(150))
                },
                label = "StatusTransition"
            ) { active ->
                Text(
                    text = if (active) "● Active" else "○ Disabled",
                    fontFamily = FONT_MONO,
                    fontSize = 11.sp,
                    color = if (active) ACCENT_NEON else TEXT_MUTED
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Switch(
                checked = isEnabled,
                onCheckedChange = entry.onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = ACCENT_NEON,
                    uncheckedTrackColor = BORDER_SUBTLE
                )
            )
        }
    }
}
