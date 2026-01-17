package com.example.goalcoach.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.example.goalcoach.room.GoalEntity


@Singleton
class FirestoreGoalRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // Save or update a goal in Firestore
    suspend fun upsertGoal(userId: String, goal: GoalEntity) {
        try {
            firestore
                .collection("users")
                .document(userId)
                .collection("goals")
                .document(goal.id)
                .set(goal, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            // Log error but don't crash - local data is still safe
            e.printStackTrace()
        }
    }

    // Delete a goal from Firestore
    suspend fun deleteGoal(userId: String, goalId: String) {
        try {
            firestore
                .collection("users")
                .document(userId)
                .collection("goals")
                .document(goalId)
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fetch all goals for a user from Firestore
    suspend fun fetchGoals(userId: String): List<GoalEntity> {
        return try {
            firestore
                .collection("users")
                .document(userId)
                .collection("goals")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(GoalEntity::class.java)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}