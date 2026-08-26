package com.orion.atena.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.orion.atena.data.AppDatabase
import com.orion.atena.data.ChatMessage
import com.orion.atena.data.ChatSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).chatDao()

    private val _currentSessionId = MutableStateFlow<String>(UUID.randomUUID().toString())
    val currentSessionId = _currentSessionId.asStateFlow()

    private val _quotedText = MutableStateFlow<String?>(null)
    val quotedText = _quotedText.asStateFlow()

    val sessions: StateFlow<List<ChatSession>> = dao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentMessages: StateFlow<List<ChatMessage>> = _currentSessionId.flatMapLatest { id ->
        dao.getMessagesForSession(id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun startNewChat() {
        _currentSessionId.value = UUID.randomUUID().toString()
        _quotedText.value = null
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        _quotedText.value = null
    }

    fun setQuotedText(text: String?) {
        _quotedText.value = text
    }

    fun sendMessage(userPrompt: String, systemPrompt: String, apiKey: String, model: String, temperature: Float) {
        if (userPrompt.isBlank()) return

        val textToSend = if (!_quotedText.value.isNullOrBlank()) {
            "> ${_quotedText.value?.replace("\n", "\n> ")}\n\n$userPrompt"
        } else {
            userPrompt
        }

        val sessionId = _currentSessionId.value
        val userMsg = ChatMessage(
            sessionId = sessionId,
            sender = "USER",
            text = textToSend,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            dao.insertSession(ChatSession(id = sessionId, title = userPrompt.take(30), updatedAt = System.currentTimeMillis()))
            dao.insertMessage(userMsg)
            _quotedText.value = null
        }
    }

    fun copyToClipboard(text: String) {
        if (text.isBlank()) return
        try {
            val context = getApplication<Application>().applicationContext
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Atena Mensagem", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Texto copiado!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(getApplication(), "Erro ao copiar texto", Toast.LENGTH_SHORT).show()
        }
    }
}
