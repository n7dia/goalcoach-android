package com.example.goalcoach.authentication

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth


// Repository responsible for authentication using FirebaseAuth
// Exposes auth state to ViewModels as a reactive Flow
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    // Current signed-in user (may be null if signed out)
    val currentUser get() = auth.currentUser

    // Emits the current user ID whenever auth state changes
    // Used by ViewModels to react to sign-in / sign-out events
    val uidFlow = callbackFlow<String?> {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // Signs out the current user
    fun signOut() = auth.signOut()

    // Signs in an existing user
    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    // Creates a new user account and signs them in
    suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }
}
