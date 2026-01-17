package com.example.goalcoach.room

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.goalcoach.models.JournalEntry


@Dao
interface JournalDao {

    // Insert or update a journal entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: JournalEntryEntity)

    // Observe all journal entries for a user (newest first)
    @Query("""
        SELECT * FROM journal_entries
        WHERE user_id = :userId
        ORDER BY date_submitted DESC
    """)
    fun observeEntriesForUser(userId: String): Flow<List<JournalEntryEntity>>

    // Observe journal entries for a specific goal
    @Query("""
        SELECT * FROM journal_entries
        WHERE user_id = :userId AND goal_id = :goalId
        ORDER BY date_submitted DESC
    """)
    fun observeEntriesForGoal(
        userId: String,
        goalId: String
    ): Flow<List<JournalEntryEntity>>

    // Delete a journal entry by id
    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun delete(entryId: String)

    // Select entries with a confidence value
    @Query("""
    SELECT * FROM journal_entries
    WHERE user_id = :userId
      AND confidence IS NOT NULL
      AND date_submitted >= :fromMs
    ORDER BY date_submitted ASC
""")
    fun observeConfidenceEntriesSince(userId: String, fromMs: Long): Flow<List<JournalEntryEntity>>

}