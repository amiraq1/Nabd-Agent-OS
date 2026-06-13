package com.nabd.ai.local.mtp_engine.data.repository

import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.architecture.Participant
import com.nabd.ai.local.mtp_engine.architecture.MessageStatus
import com.nabd.ai.local.mtp_engine.architecture.SelectedAttachment
import com.nabd.ai.local.mtp_engine.data.local.MessageEntity
import com.nabd.ai.local.mtp_engine.data.local.MessageEmbeddingEntity
import com.nabd.ai.local.mtp_engine.data.local.MtpChatDao
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
     * إنشاء أو تحديث المحادثة لتجنب مشاكل Foreign Key Constraint
     */
    suspend fun createConversation(id: String, title: String = "New Chat") {
        chatDao.upsertConversation(
            com.nabd.ai.local.mtp_engine.data.local.ChatEntity(
                id = id,
                title = title,
                selectedBranchesJson = "{}",
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    /**
     * تحميل رسائل المحادثة وتحويلها فوراً إلى نماذج واجهة المستخدم (Domain Models)
     */
    suspend fun loadMessages(chatId: String): List<ChatMessage> {
        return chatDao.getConversationMessages(chatId).map { it.toDomainModel() }
    }

    suspend fun getMessagesByIds(ids: List<String>): List<ChatMessage> {
        return chatDao.getMessagesByIds(ids).map { it.toDomainModel() }
    }

    suspend fun getIndexableMessageCount(): Int {
        return chatDao.getAllEmbeddings().size // Approximation or add a new Dao method
    }

    /**
     * حفظ رسالة فردية بـ O(1)
     */
    suspend fun saveMessage(message: ChatMessage, chatId: String) {
        chatDao.upsertMessage(message.toEntity(chatId))
    }

    suspend fun saveEmbedding(messageId: String, vector: FloatArray) {
        val jsonArray = JSONArray()
        vector.forEach { jsonArray.put(it.toDouble()) }
        chatDao.upsertEmbedding(MessageEmbeddingEntity(messageId, jsonArray.toString()))
    }

    suspend fun getAllEmbeddings(): List<MessageEmbeddingEntity> {
        return chatDao.getAllEmbeddings()
    }
    
    suspend fun getEmbeddingsByModel(modelId: String): List<MessageEmbeddingEntity> {
        // Assuming Nabd doesn't filter by modelId yet in MtpChatDao, just return all.
        return chatDao.getAllEmbeddings()
    }

    /**
     * مزامنة خريطة التوجيه عند كل تفرع جديد
     */
    suspend fun syncRoutingMap(chatId: String, branches: Map<String, String>) {
        val branchesJson = serializeToJson(branches) 
        chatDao.updateRoutingMap(chatId, branchesJson)
    }

    /**
     * دوال مساعدة للتحويل (Mappers) تبقي الكود نظيفاً
     */
    private fun MessageEntity.toDomainModel(): ChatMessage {
        val parsedAttachments: List<SelectedAttachment> = this.attachmentsJson?.let {
            try { Json.decodeFromString(it) } catch (e: Exception) { emptyList() }
        } ?: emptyList()
        val parsedStatus = this.status?.let {
            try { MessageStatus.valueOf(it) } catch (e: Exception) { MessageStatus.SUCCESS }
        } ?: MessageStatus.SUCCESS
        
        return ChatMessage(
            id = this.id,
            text = this.text,
            participant = Participant.valueOf(this.participant),
            parentId = this.parentId,
            status = parsedStatus,
            isPending = false, // Persistent messages are never pending
            thoughts = this.thoughts,
            thoughtTitle = this.thoughtTitle,
            modelName = this.modelName,
            attachments = parsedAttachments,
            timestamp = this.timestamp
        )
    }

    private fun ChatMessage.toEntity(chatId: String): MessageEntity {
        val attachmentsJsonStr = if (this.attachments.isNotEmpty()) Json.encodeToString(this.attachments) else null
        return MessageEntity(
            id = this.id,
            chatId = chatId,
            parentId = this.parentId,
            text = this.text,
            participant = this.participant.name,
            timestamp = this.timestamp,
            status = this.status.name,
            thoughts = this.thoughts,
            thoughtTitle = this.thoughtTitle,
            modelName = this.modelName,
            attachmentsJson = attachmentsJsonStr
        )
    }

    private fun serializeToJson(map: Map<String, String>): String {
        return JSONObject(map).toString()
    }
}
