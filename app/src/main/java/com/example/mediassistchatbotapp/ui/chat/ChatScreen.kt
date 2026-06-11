package com.example.mediassistchatbotapp.ui.chat

// ChatScreen.kt
// Main chat UI with:
//   - Top bar: role badge + logout button
//   - Scrollable message list
//   - Each bot message shows sources + retrieval type badge
//   - Bottom input bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

// Role badge colors
fun roleColor(role: String): Color = when (role) {
    "doctor"            -> Color(0xFF1565C0)
    "nurse"             -> Color(0xFF2E7D32)
    "billing_executive" -> Color(0xFFE65100)
    "technician"        -> Color(0xFF6A1B9A)
    "admin"             -> Color(0xFF37474F)
    else                -> Color(0xFF455A64)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onLogout: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val listState  = rememberLazyListState()
    val scope      = rememberCoroutineScope()
    var inputText  by remember { mutableStateOf("") }

    // Load user info and collections when screen opens
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Scaffold(
        // ── Top Bar ───────────────────────────────────────────────────────────
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🏥 MediBot", fontWeight = FontWeight.Bold)
                        // Show role badge and accessible collections
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape  = RoundedCornerShape(4.dp),
                                color  = roleColor(uiState.role),
                            ) {
                                Text(
                                    text     = uiState.role.replace("_", " ").uppercase(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color    = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text  = uiState.collections.joinToString(", "),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },

        // ── Bottom Input Bar ──────────────────────────────────────────────────
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = inputText,
                    onValueChange = { inputText = it },
                    placeholder   = { Text("Ask a question...") },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    shape         = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                // Send button — disabled while loading
                IconButton(
                    onClick  = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    enabled  = !uiState.isLoading && inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color  = MaterialTheme.colorScheme.primary,
                            shape  = RoundedCornerShape(24.dp)
                        )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }

    ) { paddingValues ->

        // ── Message List ──────────────────────────────────────────────────────
        LazyColumn(
            state          = listState,
            modifier       = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(uiState.messages) { message ->
                MessageBubble(message)
            }

            // Typing indicator while waiting for response
            if (uiState.isLoading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                "MediBot is thinking...",
                                modifier = Modifier.padding(12.dp),
                                style    = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    // User messages on right, bot messages on left
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(modifier = Modifier.widthIn(max = 300.dp)) {

            // ── Message text bubble ───────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(
                    topStart    = 16.dp,
                    topEnd      = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd   = if (message.isUser) 4.dp else 16.dp
                ),
                color = if (message.isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text     = message.text,
                    modifier = Modifier.padding(12.dp),
                    color    = if (message.isUser) Color.White
                    else MaterialTheme.colorScheme.onSurface,
                    style    = MaterialTheme.typography.bodyMedium
                )
            }

            // ── Source citations (bot messages only) ──────────────────────────
            if (message.sources.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            "Sources",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        message.sources.forEach { source ->
                            Text(
                                text  = "📄 ${source.sourceDocument} · ${source.sectionTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Retrieval type badge ──────────────────────────────────────────
            if (message.retrievalType.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (message.retrievalType == "sql_rag")
                        Color(0xFFFFF3E0)   // amber tint for SQL RAG
                    else
                        Color(0xFFE8F5E9)   // green tint for Hybrid RAG
                ) {
                    Text(
                        text     = if (message.retrievalType == "sql_rag") "🗃 SQL RAG"
                        else "🔍 Hybrid RAG",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color    = if (message.retrievalType == "sql_rag")
                            Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}