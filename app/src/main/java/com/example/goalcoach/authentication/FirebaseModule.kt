package com.example.goalcoach.authentication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.components.SingletonComponent
import dagger.hilt.InstallIn
import dagger.Module
import dagger.Provides
import jakarta.inject.Singleton

// Provides Firebase dependencies for the app
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    // Creates a single FirebaseAuth instance used across the app
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}