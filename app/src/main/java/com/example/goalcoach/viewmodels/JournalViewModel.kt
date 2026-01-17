package com.example.goalcoach.viewmodels

import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.goalcoach.authentication.AuthRepository
import com.example.goalcoach.models.JournalEntry
import com.example.goalcoach.room.JournalRepository


@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repo: JournalRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    // Save a new journal entry
    fun saveEntry(
        goalId: String?,
        entry: String,
        confidence: Int?
    ) {
        // Require a signed-in user
        val userId = authRepo.currentUser?.uid ?: return

        // Clean and validate text
        val text = entry.trim()
        if (text.isBlank()) return

        val newEntry = JournalEntry(
            id = UUID.randomUUID().toString(),
            userId = userId,
            goalId = goalId,                 // Optional goal link
            entry = text,
            confidence = confidence,         // Optional confidence
            dateSubmitted = System.currentTimeMillis()
        )

        // Save entry to database
        viewModelScope.launch {
            repo.upsert(newEntry)
        }
    }
}