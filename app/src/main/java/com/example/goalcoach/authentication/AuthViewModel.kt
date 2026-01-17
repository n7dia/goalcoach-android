package com.example.goalcoach.authentication

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel


// ViewModel responsible for authentication UI state and actions.
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    // Backing state initialized from the current Firebase user
    private val _state = MutableStateFlow(
        AuthUiState(
            isLoggedIn = repo.currentUser != null,
            userId = repo.currentUser?.uid,
            userEmail = repo.currentUser?.email
        )
    )

    // Public immutable state observed by the UI
    val state: StateFlow<AuthUiState> = _state

    // Update email input
    fun onEmailChange(v: String) = _state.value.let { _state.value = it.copy(email = v) }

    // Update password input
    fun onPasswordChange(v: String) = _state.value.let { _state.value = it.copy(password = v) }

    // Sign in an existing user
    fun signIn() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)

        try {
            // Firebase login
            repo.signIn(
                _state.value.email.trim(),
                _state.value.password
            )

            // Read authenticated user and update state
            val user = repo.currentUser
            _state.value = _state.value.copy(
                isLoading = false,
                isLoggedIn = true,
                userId = user?.uid,
                userEmail = user?.email
            )

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message
            )
        }
    }

    // Create a new account and sign the user in
    fun signUp() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)

        try {
            // Firebase signup
            repo.signUp(
                _state.value.email.trim(),
                _state.value.password
            )

            // Read authenticated user and update state
            val user = repo.currentUser
            _state.value = _state.value.copy(
                isLoading = false,
                isLoggedIn = true,
                userId = user?.uid,
                userEmail = user?.email
            )

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message
            )
        }
    }

    // Sign out and reset UI state (resets isLoggedIn, userId, userEmail, etc.)
    fun signOut() {
        repo.signOut()
        _state.value = AuthUiState()
    }
}