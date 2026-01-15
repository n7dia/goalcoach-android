package com.example.goalcoach.models


data class JournalEntry(
    val id: String,
    val userId: String,
    val goalId: String?,
    val entry: String,
    val confidence: Int?,
    val dateSubmitted: Long
)