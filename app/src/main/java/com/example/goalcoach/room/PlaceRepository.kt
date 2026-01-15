package com.example.goalcoach.room

import com.example.goalcoach.models.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PlaceRepository @Inject constructor(
    private val dao: PlaceDao
) {
    fun observeSavedPlacesForUser(userId: String): Flow<List<Place>> =
        dao.observeSavedPlacesForUser(userId)
            .map { list -> list.map { it.toDomain() } }

    suspend fun upsert(userId: String, place: Place) {
        dao.upsert(place.toEntity(userId))
    }

    suspend fun delete(placeId: String) {
        dao.delete(placeId)
    }

    suspend fun deleteAllForUser(userId: String) {
        dao.deleteAllForUser(userId)
    }
}