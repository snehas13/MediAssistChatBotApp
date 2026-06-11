package com.example.mediassistchatbotapp.ui.chat

// ChatViewModel.kt
// Manages chat state: message history, loading, collections
// Loads token from DataStore so user stays logged in across restarts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediassistchatbotapp.data.TokenStore
import com.example.mediassistchatbotapp.data.api.SourceCitation
import com.example.mediassistchatbotapp.data.repository.MediBotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// A single message in the chat history
data class ChatMessage(
    val isUser: Boolean,           // true = user bubble, false = bot bubble
    val text: String,
    val sources: List<SourceCitation> = emptyList(),
    val retrievalType: String = "" // "hybrid_rag" or "sql_rag"
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val collections: List<String>  = emptyList(),
    val role: String               = "",
    val username: String           = "",
    val isLoading: Boolean         = false,
    val error: String?             = null
)

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MediBotRepository()
    private val tokenStore = TokenStore(app)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    // Called when chat screen first opens
    fun initialize() {
        viewModelScope.launch {
            val role     = tokenStore.getRole() ?: return@launch
            val username = tokenStore.getUsername() ?: ""

            // Update state with user info
            _uiState.value = _uiState.value.copy(
                role     = role,
                username = username,
                messages = listOf(
                    ChatMessage(
                        isUser = false,
                        text   = "Hi $username! I'm MediBot. Ask me anything from your accessible collections."
                    )
                )
            )

            // Load which collections this role can access
            repository.getCollections(role)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(collections = response.collections)
                }
        }
    }

    fun sendMessage(question: String) {
        viewModelScope.launch {
            val token = tokenStore.getToken() ?: return@launch

            // Add user message to chat immediately (optimistic UI)
            _uiState.value = _uiState.value.copy(
                messages   = _uiState.value.messages + ChatMessage(isUser = true, text = question),
                isLoading  = true,
                error      = null
            )

            repository.chat(question, token)
                .onSuccess { response ->
                    // Add bot response
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + ChatMessage(
                            isUser        = false,
                            text          = response.answer,
                            sources       = response.sources,
                            retrievalType = response.retrievalType
                        ),
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + ChatMessage(
                            isUser = false,
                            text   = "Error: ${error.message}"
                        ),
                        isLoading = false
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenStore.clearSession()
        }
    }
}