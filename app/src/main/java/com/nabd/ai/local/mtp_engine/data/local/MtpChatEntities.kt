package com.nabd.ai.local.mtp_engine.data.local

import androidx.room.*

/**
 * ChatEntity: Represents a conversation session.
 * Stores the routing map for non-linear history exploration.
 */
@Entity(tableName = "conversations")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val selectedBranchesJson: String, // Flat JSON mapping of parentId -> selectedChildId
    val lastUpdated: Long // Epoch timestamp for sorting
)

/**
 * MessageEntity: Represents an individual message within a conversation.
 * parentId forms the relational tree structure.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE // Root-and-branch cleanup
        )
    ],
    indices = [Index("chatId"), Index("parentId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val parentId: String?, // Points to parent message forming the tree structure
    val text: String,
    val participant: String, // String representation of Participant Enum
    val timestamp: Long,
    val status: String? = null,
    val thoughts: String? = null,
    val thoughtTitle: String? = null,
    val modelName: String? = null,
    val attachmentsJson: String? = null
)

/**
 * MessageEmbeddingEntity: Stores the semantic vector for a message.
 */
@Entity(
    tableName = "message_embeddings",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MessageEmbeddingEntity(
    @PrimaryKey val messageId: String,
    val vectorJson: String // Stored as JSON or BLOB. JSON for simplicity here.
) {
    val vector: FloatArray
        get() = org.json.JSONArray(vectorJson).let { arr ->
            FloatArray(arr.length()) { arr.getDouble(it).toFloat() }
        }
}

/**
 * MtpChatDao: Data Access Object for Modular Task Processor (MTP) chat storage.
 */
@Dao
interface MtpChatDao {
    @Query("SELECT * FROM conversations ORDER BY lastUpdated DESC")
    suspend fun getAllConversations(): List<ChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ChatEntity)

    @Query("UPDATE conversations SET selectedBranchesJson = :branches WHERE id = :chatId")
    suspend fun updateRoutingMap(chatId: String, branches: String)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getConversationMessages(chatId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id IN (:ids)")
    suspend fun getMessagesByIds(ids: List<String>): List<MessageEntity>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: MessageEntity)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmbedding(embedding: MessageEmbeddingEntity)

    @Query("SELECT * FROM message_embeddings")
    suspend fun getAllEmbeddings(): List<MessageEmbeddingEntity>
}
