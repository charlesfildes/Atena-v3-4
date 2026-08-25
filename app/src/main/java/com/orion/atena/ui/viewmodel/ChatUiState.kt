package com.orion.atena.ui.viewmodel

import com.orion.atena.data.model.ChatMessage
import com.orion.atena.data.model.ChatSession

sealed interface ChatUiState {
    data object Loading : ChatUiState

    data class Active(
        val chatId: String,
        val messages: List<ChatMessage>,
        val isStreaming: Boolean = false,
        val streamingText: String = "",
        val selectedText: String? = null,
        val quotingMessage: ChatMessage? = null,
    ) : ChatUiState

    data class Error(
        val message: String,
        val chatId: String? = null,
    ) : ChatUiState
}

data class ChatHistoryState(
    val chats: List<ChatSession> = emptyList(),
    val currentChatId: String? = null,
)
