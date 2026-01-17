package com.example.goalcoach.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "journal_entries"
)
data class JournalEntryEntity(

    // Unique id for this journal entry
    @PrimaryKey
    val id: String,

    // Owner of the journal entry
    @ColumnInfo(name = "user_id")
    val userId: String,

    // Optional goal this entry is linked to
    @ColumnInfo(name = "goal_id")
    val goalId: String?,

    // Journal text content
    @ColumnInfo(name = "entry")
    val entry: String,

    // Optional confidence rating (0–10)
    @ColumnInfo(name = "confidence")
    val confidence: Int?,

    // Time the entry was created (epoch millis)
    @ColumnInfo(name = "date_submitted")
    val dateSubmitted: Long
)

