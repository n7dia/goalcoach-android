package com.example.goalcoach.models

// Represents a single journal entry created by a user
// Optional goal and confidence level selection
data class JournalEntry(
    val id: String,
    val userId: String,
    val goalId: String?,
    val entry: String,
    val confidence: Int?,
    val dateSubmitted: Long
)