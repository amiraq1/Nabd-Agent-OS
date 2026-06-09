package com.nabd.ai.local.ui.workspace

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FileTreeView(
    files: List<String>,
    modifier: Modifier = Modifier
) {
    // Simplified UI for displaying a list of files
    // In a real app this would be a hierarchical tree view
    Column(modifier = modifier) {
        Text("Workspace Files", style = MaterialTheme.typography.titleMedium)
        files.forEach { file ->
            Text(file)
        }
    }
}
