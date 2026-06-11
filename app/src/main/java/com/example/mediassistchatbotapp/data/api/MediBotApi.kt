package com.example.mediassistchatbotapp.data.api

// MediBotApi.kt
// Defines all API calls as Kotlin suspend functions
// Retrofit generates the actual HTTP implementation automatically
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MediBotApi {

    // POST /login — returns JWT token + role
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // POST /chat — main RAG endpoint, returns answer + sources
    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    // GET /collections/{role} — which collections this role can access
    @GET("collections/{role}")
    suspend fun getCollections(@Path("role") role: String): CollectionsResponse
}