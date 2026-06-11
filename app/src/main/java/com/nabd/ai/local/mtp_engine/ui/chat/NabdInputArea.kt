package com.nabd.ai.local.mtp_engine.ui.chat

import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabd.ai.R
import com.nabd.ai.local.mtp_engine.architecture.SelectedAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NabdInputArea(
    onSendMessage: (String, List<SelectedAttachment>) -> Boolean,
    onStopGeneration: () -> Unit = {},
    isLoading: Boolean,
    enabledModels: Set<String>,
    selectedModel: String,
    webSearchEnabled: Boolean = false,
    shellEnabled: Boolean = false,
    memoryEnabled: Boolean = true,
    onWebSearchToggle: (Boolean) -> Unit = {},
    onShellToggle: (Boolean) -> Unit = {},
    onMemoryToggle: (Boolean) -> Unit = {},
    onModelSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onCollapse: () -> Unit = {},
    onExpand: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedAttachments by remember { mutableStateOf<List<SelectedAttachment>>(emptyList()) }
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    val focusRequester = remember { FocusRequester() }
    var showToolBar by remember { mutableStateOf(false) }

    // البنية التكتيكية للحواف الحادة
    val tacticalShape = RoundedCornerShape(2.dp)

    // تأثير النبض الخطي (Tactical Pulse Animation) للمجال الحيوي للنظام
    val pulseTransition = rememberInfiniteTransition(label = "TacticalPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    BackHandler(enabled = isExpanded) { onCollapse() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isExpanded) Modifier.fillMaxHeight() else Modifier)
            .background(MaterialTheme.colorScheme.surface, tacticalShape)
            .padding(4.dp)
    ) {
        // مؤشر النبض التكتيكي العلوي - يظهر فقط أثناء المعالجة والتوليد
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .alpha(pulseAlpha)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        // منطقة عرض المرفقات الحالية
        if (selectedAttachments.isNotEmpty() && !isExpanded) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(selectedAttachments.size) { index ->
                    val attachment = selectedAttachments[index]
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(tacticalShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = attachment.type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.TopEnd)
                                .clickable {
                                    selectedAttachments = selectedAttachments.toMutableList().also { it.removeAt(index) }
                                }
                        )
                    }
                }
            }
        }

        // حقل الإدخال النصي الرئيسي بأسلوب صارم وحواف تكتيكية
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isExpanded) Modifier.weight(1f) else Modifier)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), tacticalShape)
                .padding(4.dp)
        ) {
            TextField(
                value = textState,
                onValueChange = { textState = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        "ENTER DIRECTIVE...", 
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                maxLines = if (isExpanded) Int.MAX_VALUE else 5,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }

        // شريط الأدوات المدمج والرقاقات التكتيكية (Tactical Toolbar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showToolBar = !showToolBar },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (showToolBar) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Toggle Tools",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(
                    visible = showToolBar,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        TacticalToolChip(
                            label = "WEB",
                            icon = Icons.Default.Language,
                            isActive = webSearchEnabled,
                            onClick = { onWebSearchToggle(!webSearchEnabled) }
                        )
                        TacticalToolChip(
                            label = "SHELL",
                            icon = Icons.Default.Terminal,
                            isActive = shellEnabled,
                            onClick = { onShellToggle(!shellEnabled) }
                        )
                        TacticalToolChip(
                            label = "MEMORY",
                            icon = Icons.Default.Psychology,
                            isActive = memoryEnabled,
                            onClick = { onMemoryToggle(!memoryEnabled) }
                        )
                    }
                }
            }

            val canSend = textState.text.isNotBlank() || selectedAttachments.isNotEmpty()
            
            IconButton(
                onClick = {
                    if (isLoading) {
                        onStopGeneration()
                    } else if (canSend) {
                        if (onSendMessage(textState.text, selectedAttachments)) {
                            selectedAttachments = emptyList()
                            textState = TextFieldValue("")
                        }
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isLoading) Color.Red else if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        tacticalShape
                    )
            ) {
                Icon(
                    imageVector = if (isLoading) Icons.Default.Stop else Icons.Default.ArrowUpward,
                    contentDescription = "Execute Directive",
                    tint = if (isLoading || canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TacticalToolChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                RoundedCornerShape(2.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(12.dp),
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
