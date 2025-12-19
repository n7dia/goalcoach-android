package com.example.goalcoach.room

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    fun observeGoalsForUser(userId: String): Flow<List<GoalEntity>> =
        goalDao.observeGoalsForUser(userId)

    suspend fun upsert(goal: GoalEntity) = goalDao.upsert(goal)

    suspend fun delete(goalId: String) = goalDao.deleteById(goalId)
}