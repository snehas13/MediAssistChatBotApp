package com.example.mediassistchatbotapp.data

// TokenStore.kt
// Stores JWT token and role using DataStore (modern replacement for SharedPreferences)
// Token persists across app restarts

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension property — creates a single DataStore instance per app
val Context.dataStore by preferencesDataStore(name = "medibot_prefs")

class TokenStore(private val context: Context) {

    companion object {
        val KEY_TOKEN    = stringPreferencesKey("token")
        val KEY_ROLE     = stringPreferencesKey("role")
        val KEY_USERNAME = stringPreferencesKey("username")
    }

    // Save login data after successful login
    suspend fun saveSession(token: String, role: String, username: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN]    = token
            prefs[KEY_ROLE]     = role
            prefs[KEY_USERNAME] = username
        }
    }

    // Read token (returns null if not logged in)
    suspend fun getToken(): String? =
        context.dataStore.data.map { it[KEY_TOKEN] }.first()

    suspend fun getRole(): String? =
        context.dataStore.data.map { it[KEY_ROLE] }.first()

    suspend fun getUsername(): String? =
        context.dataStore.data.map { it[KEY_USERNAME] }.first()

    // Clear on logout
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}