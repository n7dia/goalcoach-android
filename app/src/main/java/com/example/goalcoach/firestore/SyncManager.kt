package com.example.goalcoach.firestore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.example.goalcoach.authentication.AuthRepository
import com.example.goalcoach.room.GoalDao
import com.example.goalcoach.room.JournalDao
import com.example.goalcoach.room.PlaceDao

/**
 * Manages sync between Room (local) and Firestore (cloud)
 * - Pulls cloud data on sign-in
 * - Pushes local changes to cloud automatically
 */
@Singleton
class SyncManager @Inject constructor(
    private val authRepo: AuthRepository,
    private val goalDao: GoalDao,
    private val journalDao: JournalDao,
    private val placeDao: PlaceDao,
    private val firestoreGoals: FirestoreGoalRepository,
    private val firestoreJournal: FirestoreJournalRepository,
    private val firestorePlaces: FirestorePlaceRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Call this when the app starts
    fun initialize() {
        // Listen for auth state changes
        scope.launch {
            authRepo.uidFlow.collect { userId ->
                if (userId != null) {
                    // User signed in - sync data
                    syncFromCloud(userId)
                }
                // Note: We don't clear local data on sign-out
                // so users can still view their data offline
            }
        }
    }

    // Pull data from Firestore and merge with local Room database
    private suspend fun syncFromCloud(userId: String) {
        try {
            // Fetch goals from Firestore
            val cloudGoals = firestoreGoals.fetchGoals(userId)
            cloudGoals.forEach { goal ->
                goalDao.upsert(goal)
            }

            // Fetch journal entries from Firestore
            val cloudEntries = firestoreJournal.fetchEntries(userId)
            cloudEntries.forEach { entry ->
                journalDao.upsert(entry)
            }

            // Fetch places from Firestore
            val cloudPlaces = firestorePlaces.fetchPlaces(userId)
            cloudPlaces.forEach { place ->
                placeDao.upsert(place)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Sync failed but local data is still available
        }
    }

    // Push a goal to Firestore (call this after saving to Room)
    suspend fun pushGoal(userId: String, goal: com.example.goalcoach.room.GoalEntity) {
        firestoreGoals.upsertGoal(userId, goal)
    }

    // Push a journal entry to Firestore (call this after saving to Room)
    suspend fun pushJournalEntry(userId: String, entry: com.example.goalcoach.room.JournalEntryEntity) {
        firestoreJournal.upsertEntry(userId, entry)
    }

    // Delete goal from Firestore (call this after deleting from Room)
    suspend fun deleteGoal(userId: String, goalId: String) {
        firestoreGoals.deleteGoal(userId, goalId)
    }

    // Delete journal entry from Firestore (call this after deleting from Room)
    suspend fun deleteJournalEntry(userId: String, entryId: String) {
        firestoreJournal.deleteEntry(userId, entryId)
    }

    // Push a place to Firestore (call this after saving to Room)
    suspend fun pushPlace(userId: String, place: com.example.goalcoach.room.PlaceEntity) {
        firestorePlaces.upsertPlace(userId, place)
    }

    // Delete place from Firestore (call this after deleting from Room)
    suspend fun deletePlace(userId: String, placeId: String) {
        firestorePlaces.deletePlace(userId, placeId)
    }

    // Delete all places from Firestore (call this after deleting all from Room)
    suspend fun deleteAllPlaces(userId: String) {
        firestorePlaces.deleteAllPlaces(userId)
    }
}