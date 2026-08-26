package com.orion.atena.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.currentMessages.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val quotedText by viewModel.quotedText.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showHistoryMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Atena • Orion") },
                actions = {
                    IconButton(onClick = { viewModel.startNewChat() }) {
                        Icon(Icons.Default.AddComment, contentDescription = "Novo Chat")
                    }
                    IconButton(onClick = { showHistoryMenu = !showHistoryMenu }) {
                        Icon(Icons.Default.History, contentDescription = "Histórico")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showHistoryMenu) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Histórico Recente", style = MaterialTheme.typography.titleMedium)
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(sessions) { session ->
                                TextButton(
                                    onClick = {
                                        viewModel.selectSession(session.id)
                                        showHistoryMenu = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(session.title, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(
                        messageText = msg.text,
                        isUser = msg.sender == "USER",
                        onCopy = { viewModel.copyToClipboard(msg.text) },
                        onQuote = { selectedOrFullText ->
                            viewModel.setQuotedText(selectedOrFullText)
                        }
                    )
                }
            }

            quotedText?.let { quote ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FormatQuote, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = quote,
                        maxLines = 2,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.setQuotedText(null) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remover citação")
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Digite sua mensagem...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(
                            userPrompt = inputText,
                            systemPrompt = "",
                            apiKey = "",
                            model = "DeepSeek V3",
                            temperature = 0.7f
                        )
                        inputText = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    messageText: String,
    isUser: Boolean,
    onCopy: () -> Unit,
    onQuote: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                SelectionContainer {
                    Text(text = messageText)
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { onQuote(messageText) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.FormatQuote, contentDescription = "Citar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar")
                    }
                }
            }
        }
    }
}
