package com.nabd.ai.local.ui.autonomy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val haptic = LocalHapticFeedback.current
    val isAgentRunning = state == AutonomousExecutionState.EXECUTING || 
                         state == AutonomousExecutionState.PLANNING || 
                         state == AutonomousExecutionState.REFLECTING
    val isAgentPaused = state == AutonomousExecutionState.PAUSED

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // حالة العميل الذكي البصرية
        Text(
            text = when {
                isAgentPaused -> "AGENT_PAUSED"
                isAgentRunning -> "AGENT_EXECUTING"
                state == AutonomousExecutionState.COMPLETED -> "GOAL_ACHIEVED"
                state == AutonomousExecutionState.FAILED -> "SYSTEM_FAULT"
                else -> "AGENT_IDLE"
            },
            color = when {
                isAgentPaused -> MaterialTheme.colorScheme.secondary
                isAgentRunning -> MaterialTheme.colorScheme.primary
                state == AutonomousExecutionState.COMPLETED -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            },
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (plan != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GOAL :: ${plan.originalGoal.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = if (plan.steps.isNotEmpty()) {
                            plan.steps.count { it.state == com.nabd.ai.local.autonomy.planning.StepState.COMPLETED }.toFloat() / plan.steps.size
                        } else 0f,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // -----------------------------------------------------------------
        // أزرار التحكم الديناميكية باستخدام الرسوم المتحركة السائلة
        // -----------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. زر إيقاف مؤقت (Pause)
            AnimatedVisibility(
                visible = isAgentRunning,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPause()
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("PAUSE_AGENT", fontWeight = FontWeight.Medium)
                }
            }

            // 2. زر الاستئناف الرئيسي (Resume)
            AnimatedVisibility(
                visible = isAgentPaused,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onResume()
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("RESUME_EXECUTION", fontWeight = FontWeight.Bold)
                }
            }

            // 3. زر الإلغاء النهائي (Cancel)
            AnimatedVisibility(
                visible = isAgentRunning || isAgentPaused,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCancel()
                    },
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("HALT_AGENT", fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "EXECUTION_TIMELINE",
            modifier = Modifier.align(Alignment.Start),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(timeline.reversed()) { event ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "> ${event.type.name}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Divider(
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}
