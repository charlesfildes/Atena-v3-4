package com.orion.atena.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orion.atena.data.model.ChatMessage
import com.orion.atena.ui.viewmodel.ChatViewModel
import com.orion.atena.ui.viewmodel.ChatUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val historyState by viewModel.historyState.collectAsState()
    val quotingMessage by viewModel.quotingMessage.collectAsState()
    val selectedText by viewModel.selectedText.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showHistory) {
        ModalNavigationDrawer(
            drawerContent = {
                HistorySidebar(
                    chats = historyState.chats,
                    currentChatId = historyState.currentChatId,
                    onSelectChat = { chatId ->
                        viewModel.openChat(chatId)
                        showHistory = false
                    },
                    onNewChat = {
                        viewModel.newChat()
                        showHistory = false
                    },
                    onDeleteChat = { chatId ->
                        viewModel.deleteChat(chatId)
                    },
                )
            },
        ) {
            MainChatContent(
                uiState = uiState,
                messages = messages,
                quotingMessage = quotingMessage,
                selectedText = selectedText,
                inputText = inputText,
                onInputChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText.trim())
                        inputText = ""
                    }
                },
                onCopy = { content ->
                    copyToClipboard(context, content)
                },
                onQuote = { message, text ->
                    viewModel.startQuote(message)
                    if (!text.isNullOrBlank()) {
                        viewModel.selectText(text)
                    }
                },
                onCancelQuote = { viewModel.cancelQuote() },
                onSelectText = { viewModel.selectText(it) },
                onNewChat = { viewModel.newChat() },
                onOpenHistory = { showHistory = true },
                listState = listState,
            )
        }
    } else {
        MainChatContent(
            uiState = uiState,
            messages = messages,
            quotingMessage = quotingMessage,
            selectedText = selectedText,
            inputText = inputText,
            onInputChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    viewModel.sendMessage(inputText.trim())
                    inputText = ""
                }
            },
            onCopy = { content ->
                copyToClipboard(context, content)
            },
            onQuote = { message, text ->
                viewModel.startQuote(message)
                if (!text.isNullOrBlank()) {
                    viewModel.selectText(text)
                }
            },
            onCancelQuote = { viewModel.cancelQuote() },
            onSelectText = { viewModel.selectText(it) },
            onNewChat = { viewModel.newChat() },
            onOpenHistory = { showHistory = true },
            listState = listState,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChatContent(
    uiState: ChatUiState,
    messages: List<ChatMessage>,
    quotingMessage: ChatMessage?,
    selectedText: String?,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onCopy: (String) -> Unit,
    onQuote: (ChatMessage, String?) -> Unit,
    onCancelQuote: () -> Unit,
    onSelectText: (String?) -> Unit,
    onNewChat: () -> Unit,
    onOpenHistory: () -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("🌌 Atena — Projeto Orion", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNewChat) {
                        Icon(Icons.Default.Add, contentDescription = "Novo Chat")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Default.History, contentDescription = "Histórico")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        isUser = message.role == "user",
                        isQuoting = quotingMessage?.id == message.id,
                        selectedText = if (quotingMessage?.id == message.id) selectedText else null,
                        onCopy = onCopy,
                        onQuote = { text -> onQuote(message, text) },
                        onSelectText = onSelectText,
                    )
                }

                if (uiState is ChatUiState.Active && uiState.isStreaming) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            if (quotingMessage != null) {
                QuoteBar(
                    message = quotingMessage,
                    selectedText = selectedText,
                    onCancel = onCancelQuote,
                )
            }

            InputBar(
                inputText = inputText,
                onTextChange = onInputChange,
                onSend = onSend,
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isUser: Boolean,
    isQuoting: Boolean,
    selectedText: String?,
    onCopy: (String) -> Unit,
    onQuote: (String?) -> Unit,
    onSelectText: (String?) -> Unit,
) {
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val borderColor = if (isQuoting) MaterialTheme.colorScheme.tertiary else Color.Transparent

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        if (!message.quotedContent.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .padding(bottom = 4.dp),
            ) {
                Text(
                    text = "> ${message.quotedContent}",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp,
            ),
            color = bubbleColor,
            border = if (isQuoting) {
                androidx.compose.foundation.BorderStroke(2.dp, borderColor)
            } else null,
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SelectionContainer {
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(
                onClick = { onCopy(message.content) },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copiar mensagem",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }

            IconButton(
                onClick = { onQuote(null) },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = "Citar mensagem",
                    tint = if (isQuoting) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
fun HistorySidebar(
    chats: List<com.orion.atena.data.model.ChatSession>,
    currentChatId: String?,
    onSelectChat: (String) -> Unit,
    onNewChat: () -> Unit,
    onDeleteChat: (String) -> Unit,
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp),
        ) {
            Text(
                text = "Histórico de Chats",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp),
            )

            Button(
                onClick = onNewChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Novo Chat")
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(chats, key = { it.id }) { chat ->
                    val isCurrent = chat.id == currentChatId
                    Surface(
                        color = if (isCurrent) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = chat.title,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectChat(chat.id) },
                                fontSize = 14.sp,
                                maxLines = 1,
                            )
                            IconButton(
                                onClick = { onDeleteChat(chat.id) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Excluir chat",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun QuoteBar(
    message: ChatMessage,
    selectedText: String?,
    onCancel: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = selectedText?.take(100) ?: message.content.take(100),
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                maxLines = 2,
            )
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancelar citação")
            }
        }
    }
}

@Composable
fun InputBar(
    inputText: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Digite sua mensagem...") },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
            )
            FilledIconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank(),
                modifier = Modifier.size(48.dp),
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(16.dp),
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Atena está digitando...", fontSize = 14.sp)
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Mensagem", text)
    clipboard.setPrimaryClip(clip)
}
