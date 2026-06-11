package com.nabd.ai.local.mtp_engine.data.repository

import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.architecture.Participant
import com.nabd.ai.local.mtp_engine.data.local.MessageEntity
import com.nabd.ai.local.mtp_engine.data.local.MessageEmbeddingEntity
import com.nabd.ai.local.mtp_engine.data.local.MtpChatDao
import org.json.JSONObject
import org.json.JSONArray

/**
 * ConversationRepository: Modular Task Processor (MTP) data layer implementation.
 * Intentional Minimalism - Thin Mapper & Sync Node between Room and Domain.
 */
class ConversationRepository(
    private val chatDao: MtpChatDao
) {
    /**
     * تحميل رسائل المحادثة وتحويلها فوراً إلى نماذج واجهة المستخدم (Domain Models)
     */
    suspend fun loadMessages(conversationId: String): List<ChatMessage> {
        return chatDao.getConversationMessages(conversationId).map { it.toDomainModel() }
    }

    suspend fun getMessagesByIds(ids: List<String>): List<ChatMessage> {
        return chatDao.getMessagesByIds(ids).map { it.toDomainModel() }
    }

    /**
     * حفظ رسالة فردية بـ O(1)
     */
    suspend fun saveMessage(message: ChatMessage, conversationId: String) {
        chatDao.upsertMessage(message.toEntity(conversationId))
    }

    suspend fun saveEmbedding(messageId: String, vector: FloatArray) {
        val jsonArray = JSONArray()
        vector.forEach { jsonArray.put(it.toDouble()) }
        chatDao.upsertEmbedding(MessageEmbeddingEntity(messageId, jsonArray.toString()))
    }

    suspend fun getAllEmbeddings(): List<MessageEmbeddingEntity> {
        return chatDao.getAllEmbeddings()
    }

    /**
     * مزامنة خريطة التوجيه عند كل تفرع جديد
     */
    suspend fun syncRoutingMap(conversationId: String, branches: Map<String, String>) {
        val branchesJson = serializeToJson(branches) 
        chatDao.updateRoutingMap(conversationId, branchesJson)
    }

    /**
     * دوال مساعدة للتحويل (Mappers) تبقي الكود نظيفاً
     */
    private fun MessageEntity.toDomainModel(): ChatMessage {
        return ChatMessage(
            id = this.id,
            text = this.text,
            participant = Participant.valueOf(this.participant),
            parentId = this.parentId,
            isPending = false, // Persistent messages are never pending
            timestamp = this.timestamp
        )
    }

    private fun ChatMessage.toEntity(conversationId: String): MessageEntity {
        return MessageEntity(
            id = this.id,
            conversationId = conversationId,
            parentId = this.parentId,
            text = this.text,
            participant = this.participant.name,
            timestamp = this.timestamp
        )
    }

    private fun serializeToJson(map: Map<String, String>): String {
        return JSONObject(map).toString()
    }
}
