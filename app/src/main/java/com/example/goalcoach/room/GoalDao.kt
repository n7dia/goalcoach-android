package com.example.goalcoach.room

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface GoalDao {

    // Observe all goals for a user, newest first
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY dateCreated DESC")
    fun observeGoalsForUser(userId: String): Flow<List<GoalEntity>>

    // Insert or replace a goal (used for create + update)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: GoalEntity)

    // Update an existing goal
    @Update
    suspend fun update(goal: GoalEntity)

    // Delete a goal by its id
    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteById(goalId: String)
}