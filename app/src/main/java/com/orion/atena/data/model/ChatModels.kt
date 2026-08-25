package com.orion.atena.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quotedFrom: String? = null,
    val quotedContent: String? = null,
)

@Entity(tableName = "chats")
data class ChatSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val chatId: String,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quotedFrom: String? = null,
    val quotedContent: String? = null,
)
