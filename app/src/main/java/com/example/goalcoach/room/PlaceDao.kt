package com.example.goalcoach.room

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query



@Dao
interface PlaceDao {

    // Insert or update a saved place
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(place: PlaceEntity)

    // Observe all saved places for a user (newest first)
    @Query("""
        SELECT * FROM places
        WHERE user_id = :userId
        ORDER BY date_saved DESC
    """)
    fun observeSavedPlacesForUser(userId: String): Flow<List<PlaceEntity>>

    // Delete a single place by id
    @Query("DELETE FROM places WHERE id = :placeId")
    suspend fun delete(placeId: String)

    // Delete all places for a user
    @Query("DELETE FROM places WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}