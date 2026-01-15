package com.example.goalcoach.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalcoach.authentication.AuthRepository
import com.example.goalcoach.models.JournalEntry
import com.example.goalcoach.room.JournalRepository
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repo: JournalRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    fun saveEntry(
        goalId: String?,
        entry: String,
        confidence: Int?
    ) {
        val userId = authRepo.currentUser?.uid ?: return
        val text = entry.trim()
        if (text.isBlank()) return

        val newEntry = JournalEntry(
            id = UUID.randomUUID().toString(),
            userId = userId,
            goalId = goalId,
            entry = text,
            confidence = confidence,
            dateSubmitted = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repo.upsert(newEntry)
        }
    }
}