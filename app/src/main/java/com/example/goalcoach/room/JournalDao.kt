package com.example.goalcoach.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: JournalEntryEntity)

    @Query("""
        SELECT * FROM journal_entries
        WHERE user_id = :userId
        ORDER BY date_submitted DESC
    """)
    fun observeEntriesForUser(userId: String): Flow<List<JournalEntryEntity>>

    @Query("""
        SELECT * FROM journal_entries
        WHERE user_id = :userId AND goal_id = :goalId
        ORDER BY date_submitted DESC
    """)
    fun observeEntriesForGoal(
        userId: String,
        goalId: String
    ): Flow<List<JournalEntryEntity>>

    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun delete(entryId: String)
}