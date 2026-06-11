package com.nabd.ai.local.mtp_engine.domain.generation

import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.architecture.Participant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ContextAssembler: Modular Task Processor (MTP) domain implementation.
 * Intentional Minimalism - Zero-Overhead Context Window Management.
 */
class ContextAssembler(
    private val maxContextTokens: Int = 4096 // Default maximum context size
) {
    /**
     * تجميع المسار النشط واقتطاع الرسائل القديمة لضمان عدم تجاوز نافذة السياق.
     * يعمل في خيط مستقل (Default/IO) لحماية واجهة المستخدم.
     */
    suspend fun assembleContext(
        activePath: List<ChatMessage>,
        systemPrompt: String = "You are Nabd, a highly advanced tactical AI agent."
    ): List<ChatMessage> = withContext(Dispatchers.Default) {
        val contextWindow = mutableListOf<ChatMessage>()
        var estimatedTokens = estimateTokens(systemPrompt)

        // إضافة رسالة النظام كقاعدة أساسية لا يمكن حذفها
        contextWindow.add(
            ChatMessage(
                id = "system_root",
                text = systemPrompt,
                participant = Participant.SYSTEM,
                parentId = null,
                timestamp = 0L
            )
        )

        // الاجتياز العكسي: من أحدث رسالة إلى أقدم رسالة
        for (message in activePath.reversed()) {
            val msgTokens = estimateTokens(message.text)
            
            // ترك هامش آمن (مثلاً 500 توكن) للتوليد القادم
            if (estimatedTokens + msgTokens > maxContextTokens - 500) {
                break 
            }
            
            contextWindow.add(message)
            estimatedTokens += msgTokens
        }

        // إعادة الترتيب الزمني الصحيح (النظام -> الأقدم -> الأحدث)
        // الاحتفاظ بـ system_root في الأعلى، وعكس الباقي
        val systemMsg = contextWindow.first()
        val chatHistory = contextWindow.drop(1).reversed()
        
        return@withContext listOf(systemMsg) + chatHistory
    }

    /**
     * تقدير سريع للتوكنات (O(N)) بدون استدعاء المحرك الناتيف لتجنب البطء.
     * قاعدة تقريبية: كل 4 أحرف إنجليزية أو 2 أحرف عربية = 1 توكن.
     */
    private fun estimateTokens(text: String): Int {
        // يمكن لاحقاً استبداله بحساب أدق عبر LlamaEngine إذا لزم الأمر
        return text.length / 3 
    }
}
