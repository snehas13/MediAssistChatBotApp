package com.example.mediassistchatbotapp.data.api

// ApiModels.kt
// Data classes that match exactly what the FastAPI backend sends/receives
// Gson uses these to serialize/deserialize JSON automatically

import com.google.gson.annotations.SerializedName

// ── Request bodies ────────────────────────────────────────────────────────────

data class LoginRequest(
    val username: String,
    val password: String
)

data class ChatRequest(
    val question: String,
    val token: String      // JWT token from login, sent with every chat request
)

// ── Response bodies ───────────────────────────────────────────────────────────

data class LoginResponse(
    val token: String,     // JWT token to store and reuse
    val role: String,      // e.g. "doctor", "nurse"
    val username: String
)

data class SourceCitation(
    @SerializedName("source_document") val sourceDocument: String,
    @SerializedName("section_title")   val sectionTitle: String,
    val collection: String
)

data class ChatResponse(
    val answer: String,
    val sources: List<SourceCitation>,
    @SerializedName("retrieval_type") val retrievalType: String, // "hybrid_rag" or "sql_rag"
    val role: String
)

data class CollectionsResponse(
    val role: String,
    val collections: List<String>  // e.g. ["general", "nursing"]
)