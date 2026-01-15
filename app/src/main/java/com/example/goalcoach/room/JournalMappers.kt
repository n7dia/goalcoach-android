package com.example.goalcoach.room

import com.example.goalcoach.models.JournalEntry

fun JournalEntryEntity.toDomain(): JournalEntry =
    JournalEntry(
        id = id,
        userId = userId,
        goalId = goalId,
        entry = entry,
        confidence = confidence,
        dateSubmitted = dateSubmitted
    )

fun JournalEntry.toEntity(): JournalEntryEntity =
    JournalEntryEntity(
        id = id,
        userId = userId,
        goalId = goalId,
        entry = entry,
        confidence = confidence,
        dateSubmitted = dateSubmitted
    )