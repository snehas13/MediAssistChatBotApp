package com.example.mediassistchatbotapp

// MainActivity.kt
// Sets up navigation between Login and Chat screens
// NavController handles back stack automatically

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mediassistchatbotapp.ui.chat.ChatScreen
import com.example.mediassistchatbotapp.ui.login.LoginScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MediBotApp()
            }
        }
    }
}

@Composable
fun MediBotApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        // Login screen → navigates to chat on success
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Clear back stack so user can't go back to login
                    navController.navigate("chat") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Chat screen → navigates back to login on logout
        composable("chat") {
            ChatScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("chat") { inclusive = true }
                    }
                }
            )
        }
    }
}