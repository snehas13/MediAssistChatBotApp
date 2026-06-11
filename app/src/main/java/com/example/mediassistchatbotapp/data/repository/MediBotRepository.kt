package com.example.mediassistchatbotapp.data.repository

// MediBotRepository.kt
// Single source of truth for all data operations
// ViewModels call this — never call the API directly from a ViewModel

import com.example.mediassistchatbotapp.data.api.ChatRequest
import com.example.mediassistchatbotapp.data.api.LoginRequest
import com.example.mediassistchatbotapp.data.api.RetrofitClient


class MediBotRepository {

    private val api = RetrofitClient.api

    // Login — returns Result so ViewModel can handle success/failure cleanly
    suspend fun login(username: String, password: String) = runCatching {
        api.login(LoginRequest(username, password))
    }

    // Send chat message
    suspend fun chat(question: String, token: String) = runCatching {
        api.chat(ChatRequest(question, token))
    }

    // Get accessible collections for sidebar
    suspend fun getCollections(role: String) = runCatching {
        api.getCollections(role)
    }
}