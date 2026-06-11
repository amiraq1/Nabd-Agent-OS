package com.nabd.ai.local.mtp_engine.architecture

import java.util.UUID

/**
 * Participant: Defines the entities involved in a conversation.
 */
enum class Participant {
    USER,
    ASSISTANT,
    SYSTEM,
    ERROR
}

enum class MessageStatus {
    TRANSCRIBING, SENDING, THINKING, TOOL_CALLING, SUCCESS, STOPPED, ERROR
}

/**
 * ChatMessage: Central data model for a conversation turn.
 * Optimized for Modular Task Processor (MTP) architecture.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val participant: Participant,
    val status: MessageStatus = MessageStatus.SUCCESS,
    val isPending: Boolean = false,
    val thoughts: String? = null,
    val thoughtTitle: String? = null,
    val modelName: String? = null,
    val parentId: String? = null,
    val childIds: List<String> = emptyList(),
    val attachments: List<SelectedAttachment> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
