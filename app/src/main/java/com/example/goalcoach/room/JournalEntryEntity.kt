package com.example.goalcoach.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "journal_entries"
)
data class JournalEntryEntity(

    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "goal_id")
    val goalId: String?,     // nullable = optional goal

    @ColumnInfo(name = "entry")
    val entry: String,

    @ColumnInfo(name = "confidence")
    val confidence: Int?,    // nullable = optional confidence

    @ColumnInfo(name = "date_submitted")
    val dateSubmitted: Long
)

