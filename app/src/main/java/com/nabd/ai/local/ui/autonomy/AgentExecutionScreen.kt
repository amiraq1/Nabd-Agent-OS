package com.nabd.ai.local.ui.autonomy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nabd.ai.local.autonomy.history.TimelineEvent
import com.nabd.ai.local.autonomy.planning.ExecutionPlan
import com.nabd.ai.local.autonomy.runtime.AutonomousExecutionState

@Composable
fun AgentExecutionScreen(
    state: AutonomousExecutionState,
    plan: ExecutionPlan?,
    timeline: List<TimelineEvent>,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Autonomous Agent Execution", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("State: ${state.name}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        
        if (plan != null) {
            Text("Goal: ${plan.originalGoal}", style = MaterialTheme.typography.bodyLarge)
            Text("Progress: ${plan.steps.count { it.state == com.nabd.ai.local.autonomy.planning.StepState.COMPLETED }} / ${plan.steps.size} steps")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state == AutonomousExecutionState.PAUSED) {
                Button(onClick = onResume) { Text("Resume") }
            } else if (state == AutonomousExecutionState.EXECUTING || state == AutonomousExecutionState.PLANNING || state == AutonomousExecutionState.REFLECTING) {
                Button(onClick = onPause) { Text("Pause") }
            }
            Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Cancel")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Timeline", style = MaterialTheme.typography.titleMedium)
        Divider()
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(timeline.reversed()) { event ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(event.type.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Text(event.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
