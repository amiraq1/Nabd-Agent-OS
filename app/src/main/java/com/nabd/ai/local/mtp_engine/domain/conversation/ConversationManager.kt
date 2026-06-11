package com.nabd.ai.local.mtp_engine.domain.conversation

import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ConversationManager: Modular Task Processor (MTP) domain implementation.
 * Intentional Minimalism - Flat-Mapped Message Tree & Branch Routing.
 */
class ConversationManager {
    // التكوين الهيكلي: تخزين مسطح بـ O(1) للوصول
    private val _messages = mutableMapOf<String, ChatMessage>()
    
    // خريطة التوجيه: { parentId -> selectedChildId }
    private val _selectedBranches = mutableMapOf<String, String>()

    // مسار العرض الخطي الذي تستهلكه واجهة المستخدم (UDF)
    private val _activePath = MutableStateFlow<List<ChatMessage>>(emptyList())
    val activePath: StateFlow<List<ChatMessage>> = _activePath.asStateFlow()

    private var currentRootId: String? = null

    /**
     * بناء مسار المحادثة الخطي انطلاقاً من الجذر مروراً بالفروع النشطة
     */
    fun resolveActivePath() {
        val path = mutableListOf<ChatMessage>()
        var currentId = currentRootId

        // اجتياز الشجرة بسلاسة وتجميع المسار النشط
        while (currentId != null) {
            val msg = _messages[currentId] ?: break
            path.add(msg)
            currentId = _selectedBranches[currentId]
        }
        
        _activePath.value = path
    }

    /**
     * إدراج رسالة جديدة وتحديث مسار التفرع تلقائياً
     */
    fun appendMessage(message: ChatMessage, isRoot: Boolean = false) {
        _messages[message.id] = message
        
        if (isRoot) {
            currentRootId = message.id
        } else {
            message.parentId?.let { parent ->
                // جعل الرسالة الجديدة هي الفرع النشط افتراضياً
                _selectedBranches[parent] = message.id 
            }
        }
        
        resolveActivePath()
    }

    /**
     * تحديث محتوى رسالة موجودة (مثلاً أثناء البث)
     */
    fun updateMessage(messageId: String, text: String, isPending: Boolean = false, thoughts: String? = null) {
        val existing = _messages[messageId] ?: return
        _messages[messageId] = existing.copy(text = text, isPending = isPending, thoughts = thoughts)
        resolveActivePath()
    }

    /**
     * تبديل الفرع النشط (مثال: الانتقال بين محاولات إعادة التوليد)
     */
    fun switchBranch(parentId: String, targetChildId: String) {
        if (_messages.containsKey(targetChildId)) {
            _selectedBranches[parentId] = targetChildId
            resolveActivePath()
        }
    }

    fun clear() {
        _messages.clear()
        _selectedBranches.clear()
        currentRootId = null
        _activePath.value = emptyList()
    }
}
