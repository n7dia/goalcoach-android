package com.example.goalcoach.room

import com.example.goalcoach.models.JournalEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    private val journalDao: JournalDao
) {

    suspend fun upsert(entry: JournalEntry) {
        journalDao.upsert(entry.toEntity())
    }

    fun observeEntriesForUser(userId: String): Flow<List<JournalEntry>> =
        journalDao
            .observeEntriesForUser(userId)
            .map { entities -> entities.map { it.toDomain() } }

    fun observeEntriesForGoal(
        userId: String,
        goalId: String
    ): Flow<List<JournalEntry>> =
        journalDao
            .observeEntriesForGoal(userId, goalId)
            .map { entities -> entities.map { it.toDomain() } }

    suspend fun delete(entryId: String) {
        journalDao.delete(entryId)
    }
}