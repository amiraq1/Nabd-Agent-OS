package com.nabd.ai.local.ui.workspace

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nabd.ai.local.workspace.WorkspaceEvent

@Composable
fun WorkspaceScreen(
    events: List<WorkspaceEvent>,
    files: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        FileTreeView(files = files, modifier = Modifier.weight(1f))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Recent Events", style = MaterialTheme.typography.titleMedium)
        events.takeLast(5).forEach { event ->
            Text("${event.type}: ${event.relativePath}")
        }
    }
}
