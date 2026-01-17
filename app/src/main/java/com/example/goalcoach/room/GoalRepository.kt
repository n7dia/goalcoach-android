package com.example.goalcoach.room

import com.example.goalcoach.authentication.AuthRepository
import com.example.goalcoach.firestore.SyncManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val syncManager: SyncManager,
    private val authRepo: AuthRepository
) {
    // Observe all goals for a specific user
    fun observeGoalsForUser(userId: String): Flow<List<GoalEntity>> =
        goalDao.observeGoalsForUser(userId)

    // Insert or update a goal (with cloud sync)
    suspend fun upsert(goal: GoalEntity) {
        // Save to local database first
        goalDao.upsert(goal)

        // Then sync to cloud (if user is signed in)
        val userId = authRepo.currentUser?.uid
        if (userId != null) {
            syncManager.pushGoal(userId, goal)
        }
    }

    // Delete a goal by id (with cloud sync)
    suspend fun delete(goalId: String) {
        // Delete from local database first
        goalDao.deleteById(goalId)

        // Then delete from cloud (if user is signed in)
        val userId = authRepo.currentUser?.uid
        if (userId != null) {
            syncManager.deleteGoal(userId, goalId)
        }
    }
}