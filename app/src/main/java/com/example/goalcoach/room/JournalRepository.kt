package com.example.goalcoach.room

import com.example.goalcoach.authentication.AuthRepository
import com.example.goalcoach.firestore.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.example.goalcoach.models.JournalEntry



@Singleton
class JournalRepository @Inject constructor(
    private val journalDao: JournalDao,
    private val syncManager: SyncManager,
    private val authRepo: AuthRepository
) {

    // Insert or update a journal entry (with cloud sync)
    suspend fun upsert(entry: JournalEntry) {
        val entity = entry.toEntity()

        // Save to local database first
        journalDao.upsert(entity)

        // Then sync to cloud (if user is signed in)
        val userId = authRepo.currentUser?.uid
        if (userId != null) {
            syncManager.pushJournalEntry(userId, entity)
        }
    }

    // Observe all journal entries for a user
    fun observeEntriesForUser(userId: String): Flow<List<JournalEntry>> =
        journalDao
            .observeEntriesForUser(userId)
            .map { entities -> entities.map { it.toDomain() } }

    // Observe journal entries linked to a specific goal
    fun observeEntriesForGoal(
        userId: String,
        goalId: String
    ): Flow<List<JournalEntry>> =
        journalDao
            .observeEntriesForGoal(userId, goalId)
            .map { entities -> entities.map { it.toDomain() } }

    // Delete a journal entry by id (with cloud sync)
    suspend fun delete(entryId: String) {
        // Delete from local database first
        journalDao.delete(entryId)

        // Then delete from cloud (if user is signed in)
        val userId = authRepo.currentUser?.uid
        if (userId != null) {
            syncManager.deleteJournalEntry(userId, entryId)
        }
    }

    // Observe journal entries that have confidence rating
    fun observeConfidenceEntriesSince(userId: String, fromMs: Long): Flow<List<JournalEntry>> =
        journalDao.observeConfidenceEntriesSince(userId, fromMs)
            .map { it.map { e -> e.toDomain() } }
}