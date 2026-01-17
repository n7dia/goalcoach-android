package com.example.goalcoach.authentication

// UI state for authentication screens
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val userId: String? = null,
    val userEmail: String? = null
)