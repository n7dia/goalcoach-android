package com.example.goalcoach.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(place: PlaceEntity)

    @Query("""
        SELECT * FROM places
        WHERE user_id = :userId
        ORDER BY date_saved DESC
    """)
    fun observeSavedPlacesForUser(userId: String): Flow<List<PlaceEntity>>

    @Query("DELETE FROM places WHERE id = :placeId")
    suspend fun delete(placeId: String)

    @Query("DELETE FROM places WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}