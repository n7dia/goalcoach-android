package com.example.goalcoach.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val userId: String? = null,
    val userEmail: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        AuthUiState(
            isLoggedIn = repo.currentUser != null,
            userId = repo.currentUser?.uid,
            userEmail = repo.currentUser?.email
        )
    )
    val state: StateFlow<AuthUiState> = _state

    fun onEmailChange(v: String) = _state.value.let { _state.value = it.copy(email = v) }
    fun onPasswordChange(v: String) = _state.value.let { _state.value = it.copy(password = v) }

    fun signIn() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)

        try {
            // Firebase login
            repo.signIn(
                _state.value.email.trim(),
                _state.value.password
            )

            // READ THE AUTHENTICATED USER HERE
            val user = repo.currentUser

            // STORE USER INFO IN STATE
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

    fun signUp() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)

        try {
            // Firebase signup
            repo.signUp(
                _state.value.email.trim(),
                _state.value.password
            )

            // READ THE AUTHENTICATED USER HERE
            val user = repo.currentUser

            // STORE USER INFO IN STATE
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

    fun signOut() {
        repo.signOut()
        _state.value = AuthUiState() // resets isLoggedIn, userId, userEmail, etc.
    }
}