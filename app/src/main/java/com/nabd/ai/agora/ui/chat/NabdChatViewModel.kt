package com.nabd.ai.agora.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nabd.ai.agora.data.SettingsRepository
import com.nabd.ai.agora.model.ChatMessage
import com.nabd.ai.agora.model.MessageStatus
import com.nabd.ai.agora.model.Participant
import com.nabd.ai.agora.net.CloudGatewayClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class NabdChatViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository.getInstance(application)
    private val cloudGatewayClient = CloudGatewayClient()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun sendDirective(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = trimmed,
            participant = Participant.USER,
            status = MessageStatus.SUCCESS
        )

        val assistantMessageId = UUID.randomUUID().toString()
        val assistantPlaceholder = ChatMessage(
            id = assistantMessageId,
            text = "",
            participant = Participant.MODEL,
            status = MessageStatus.SENDING
        )

        _chatMessages.update { it + userMessage + assistantPlaceholder }

        viewModelScope.launch {
            val provider = settingsRepository.activeProvider.first()
            val apiKey = when (provider.lowercase()) {
                "google" -> settingsRepository.googleApiKey.first()
                "openai" -> settingsRepository.openAiApiKey.first()
                "anthropic" -> settingsRepository.anthropicApiKey.first()
                "deepseek" -> settingsRepository.deepSeekApiKey.first()
                "qwen" -> settingsRepository.qwenApiKey.first()
                "openrouter" -> settingsRepository.openRouterApiKey.first()
                else -> settingsRepository.openAiApiKey.first()
            }
            val contextWindow = settingsRepository.contextWindowSize.first()

            cloudGatewayClient.streamChatCompletion(
                prompt = trimmed,
                provider = provider,
                apiKey = apiKey,
                contextWindow = contextWindow
            ).collect { token ->
                if (token.startsWith("❌ [GATEWAY_ERROR]")) {
                    _chatMessages.update { list ->
                        list.map { msg ->
                            if (msg.id == assistantMessageId) {
                                msg.copy(text = token, status = MessageStatus.ERROR)
                            } else msg
                        }
                    }
                } else {
                    _chatMessages.update { list ->
                        list.map { msg ->
                            if (msg.id == assistantMessageId) {
                                msg.copy(text = msg.text + token)
                            } else msg
                        }
                    }
                }
            }

            _chatMessages.update { list ->
                list.map { msg ->
                    if (msg.id == assistantMessageId && msg.status == MessageStatus.SENDING) {
                        msg.copy(status = MessageStatus.SUCCESS)
                    } else msg
                }
            }
        }
    }
}
