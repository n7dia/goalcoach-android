package com.example.goalcoach.room

import com.example.goalcoach.models.JournalEntry

// Convert Journal database entity to Journal domain model
fun JournalEntryEntity.toDomain(): JournalEntry = JournalEntry(
        id = id,
        userId = userId,
        goalId = goalId,
        entry = entry,
        confidence = confidence,
        dateSubmitted = dateSubmitted
)


// Convert Journal domain model to Journal database entity
fun JournalEntry.toEntity(): JournalEntryEntity = JournalEntryEntity(
        id = id,
        userId = userId,
        goalId = goalId,
        entry = entry,
        confidence = confidence,
        dateSubmitted = dateSubmitted
)