package com.nabd.ai.local.mtp_engine.domain.rag

import com.nabd.ai.local.engine.LlamaEngine // غلاف JNI المخصص للتضمينات (Embeddings)
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.data.repository.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

/**
 * RagManager: Modular Task Processor (MTP) domain implementation.
 * Intentional Minimalism - Zero-Overhead Semantic Search.
 */
class RagManager(
    private val embeddingEngine: LlamaEngine,
    private val repository: ConversationRepository
) {
    /**
     * بحث دلالي محلي بالكامل يعمل على خيط مستقل (IO) لحماية واجهة المستخدم.
     */
    suspend fun searchMemory(query: String, threshold: Float = 0.6f): List<ChatMessage> = withContext(Dispatchers.IO) {
        // 1. توليد التضمين (Embedding) لنص البحث عبر Llama.cpp
        val queryVector = embeddingEngine.computeEmbedding(query)
        
        // 2. سحب التضمينات المخزنة من قاعدة البيانات
        val storedEmbeddings = repository.getAllEmbeddings() 
        
        // 3. حساب التشابه (Cosine Similarity) بـ O(N) سريع وفلترة النتائج
        val results = storedEmbeddings.mapNotNull { entity ->
            val similarity = computeCosineSimilarity(queryVector, entity.vector)
            if (similarity >= threshold) Pair(entity.messageId, similarity) else null
        }.sortedByDescending { it.second }.take(5)

        // 4. استرجاع الرسائل المطابقة
        repository.getMessagesByIds(results.map { it.first })
    }

    suspend fun indexMessage(message: ChatMessage) = withContext(Dispatchers.Default) {
        // حساب التضمين للرسالة الجديدة وحفظها في الخلفية
        val vector = embeddingEngine.computeEmbedding(message.text)
        repository.saveEmbedding(message.id, vector)
    }

    private fun computeCosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        var dotProduct = 0.0f
        var norm1 = 0.0f
        var norm2 = 0.0f
        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            norm1 += vec1[i] * vec1[i]
            norm2 += vec2[i] * vec2[i]
        }
        return if (norm1 == 0.0f || norm2 == 0.0f) 0.0f else (dotProduct / (sqrt(norm1) * sqrt(norm2)))
    }
}
