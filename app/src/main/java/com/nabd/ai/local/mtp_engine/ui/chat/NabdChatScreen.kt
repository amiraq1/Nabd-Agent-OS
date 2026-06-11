package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import com.nabd.ai.local.ui.ChatViewModel
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.architecture.Participant
import com.nabd.ai.local.mtp_engine.architecture.NabdAction
import com.nabd.ai.local.mtp_engine.ui.components.RecomposeSafeMarkdown

// لوحة الألوان المخصصة من مستودع Agora لتطبيق نبض
val AgoraBackground = Color(0xFF13141A)
val AgoraSurface = Color(0xFF1E1F28)
val AgoraInputDark = Color(0xFF2A2B36)
val TextPrimary = Color.White
val TextSecondary = Color(0xFF9EA1B0)
val AccentColor = Color(0xFF3B3C46)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NabdChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // تفعيل التمرير التلقائي التكتيكي عند وصول رسائل جديدة (Auto-Scroll)
    LaunchedEffect(uiState.messages.size, uiState.isGenerating) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AgoraBackground,
        topBar = { NabdTopAppBar(currentModel = uiState.selectedModel) },
        bottomBar = {
            NabdBottomInputArea(
                text = inputText,
                onTextChange = { inputText = it },
                onSendClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.dispatch(NabdAction.ProcessPrompt(inputText))
                        inputText = ""
                    }
                },
                selectedModelName = uiState.selectedModel,
                onModelSelectClick = { /* فتح نافذة اختيار الموديل المحلي أو السحابي */ }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.messages.isEmpty()) {
                // الواجهة المركزية الأنيقة المستوحاة من الشاشة الفارغة لـ Agora
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to Nabd",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your Local Autonomous AI Agent OS",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // قائمة الرسائل المتدفقة بدقة وبدون وميض (Anti-Flash)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        NabdMessageBubble(message = message)
                    }
                }
            }
        }
    }
}

@Composable
fun NabdTopAppBar(currentModel: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // الأزرار اليسارية مدمجة داخل كبسولة ملساء كبنية Agora
        Row(
            modifier = Modifier
                .background(AgoraSurface, RoundedCornerShape(50.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* جلسة جديدة */ }) {
                Icon(Icons.Default.Add, contentDescription = "New Chat", tint = TextPrimary)
            }
            IconButton(onClick = { /* أدوات التكتيك والذاكرة */ }) {
                Icon(Icons.Default.Memory, contentDescription = "Memory Status", tint = TextPrimary)
            }
        }

        // كبسولة العنوان وشعار نبض جهة اليمين
        Row(
            modifier = Modifier
                .background(AgoraSurface, RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "نبض",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NabdBottomInputArea(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    selectedModelName: String,
    onModelSelectClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
            .background(AgoraSurface, RoundedCornerShape(28.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            // التحكم الجانبي: التوسيع وزر الإرسال الدائري السفلي
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(110.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = "Expand Panel",
                    tint = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onSendClick,
                    modifier = Modifier
                        .background(if (text.isNotBlank()) TextPrimary else AccentColor, CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Execute Command",
                        tint = if (text.isNotBlank()) AgoraBackground else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // حقل الإدخال النصي ومحدد الموديل
            Column(
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = { Text("...Ask Nabd anything", color = TextSecondary, fontSize = 15.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                // زر كبسولة النماذج (Model Picker) المستوحى بدقة من الصورة
                Row(
                    modifier = Modifier
                        .background(AgoraInputDark, RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .align(Alignment.End)
                        .clickable { onModelSelectClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Model Options", tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = selectedModelName, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Add, contentDescription = "Change Model", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun NabdMessageBubble(message: ChatMessage) {
    val isUser = message.participant == Participant.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) AccentColor else AgoraSurface,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (isUser) {
                Text(
                    text = message.text,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            } else {
                RecomposeSafeMarkdown(
                    content = message.text,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
