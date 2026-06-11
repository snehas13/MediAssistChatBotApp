package com.example.mediassistchatbotapp.ui.login

// LoginViewModel.kt
// Handles login logic, keeps UI state
// UI just observes uiState and calls login() — no business logic in the screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediassistchatbotapp.data.TokenStore
import com.example.mediassistchatbotapp.data.repository.MediBotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// All possible states the login screen can be in
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val role: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val repository  = MediBotRepository()
    private val tokenStore  = TokenStore(app)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            repository.login(username, password)
                .onSuccess { response ->
                    // Save token so chat screen can use it
                    tokenStore.saveSession(response.token, response.role, response.username)
                    _uiState.value = LoginUiState.Success(response.role)
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "Invalid credentials"
                    )
                }
        }
    }
}