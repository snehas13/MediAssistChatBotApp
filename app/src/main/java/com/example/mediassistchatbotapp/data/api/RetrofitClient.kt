package com.example.mediassistchatbotapp.data.api

// RetrofitClient.kt
// Sets up Retrofit to talk to the FastAPI backend
//
// IMPORTANT: In the Android emulator, localhost on your PC = 10.0.2.2
// On a real device on the same WiFi, use your PC's local IP e.g. 192.168.1.5

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 10.0.2.2 = your PC's localhost from inside the Android emulator
    // Change to your PC's WiFi IP if testing on a real device
    private const val BASE_URL = "http://192.168.68.102:8000/"

    // Log all HTTP requests/responses in Logcat during development
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Single Retrofit instance shared across the app
    val api: MediBotApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // JSON ↔ data classes
            .build()
            .create(MediBotApi::class.java)
    }
}