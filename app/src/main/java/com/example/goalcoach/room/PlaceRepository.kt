package com.example.goalcoach.room

import com.example.goalcoach.authentication.AuthRepository
import com.example.goalcoach.firestore.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.example.goalcoach.models.Place


@Singleton
class PlaceRepository @Inject constructor(
    private val dao: PlaceDao,
    private val syncManager: SyncManager,
    private val authRepo: AuthRepository
) {
    // Observe all saved places for a user
    fun observeSavedPlacesForUser(userId: String): Flow<List<Place>> =
        dao.observeSavedPlacesForUser(userId)
            .map { list -> list.map { it.toDomain() } }

    // Insert or update a place for a user (with cloud sync)
    suspend fun upsert(userId: String, place: Place) {
        val entity = place.toEntity(userId)

        // Save to local database first
        dao.upsert(entity)

        // Then sync to cloud (if user is signed in)
        val currentUserId = authRepo.currentUser?.uid
        if (currentUserId != null) {
            syncManager.pushPlace(currentUserId, entity)
        }
    }

    // Delete a single place by id (with cloud sync)
    suspend fun delete(placeId: String) {
        // Delete from local database first
        dao.delete(placeId)

        // Then delete from cloud (if user is signed in)
        val userId = authRepo.currentUser?.uid
        if (userId != null) {
            syncManager.deletePlace(userId, placeId)
        }
    }

    // Delete all places for a user (with cloud sync)
    suspend fun deleteAllForUser(userId: String) {
        // Delete from local database first
        dao.deleteAllForUser(userId)

        // Then delete from cloud (if user is signed in)
        val currentUserId = authRepo.currentUser?.uid
        if (currentUserId != null) {
            syncManager.deleteAllPlaces(currentUserId)
        }
    }
}