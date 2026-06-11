package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage

@Composable
fun NabdMessageList(
    messages: List<ChatMessage>,
    onEditMessage: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // مراقبة الرسائل للتمرير التلقائي التكتيكي
    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            // التمرير للعنصر الأخير بسلاسة عند وصول بيانات جديدة
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = messages,
            key = { it.id } // استخدام المعرف الفريد لضمان كفاءة الـ Recomposition
        ) { message ->
            NabdMessageItem(
                message = message,
                onEditClick = onEditMessage,
                modifier = Modifier // Removed .animateItem() as it requires newer Compose version
            )
        }
    }
}
