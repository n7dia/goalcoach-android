package com.example.goalcoach.viewmodels

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.goalcoach.authentication.AuthRepository
import com.example.goalcoach.models.Goal
import com.example.goalcoach.models.GoalCategory
import com.example.goalcoach.room.GoalRepository
import com.example.goalcoach.room.toDomain
import com.example.goalcoach.room.toEntity


@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repo: GoalRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    // Helper to get the current user id (null if signed out)
    private fun currentUserId(): String? = authRepo.currentUser?.uid

    // Goals for the signed-in user
    // Automatically switches to the correct user when auth state changes
    val goals: StateFlow<List<Goal>> =
        authRepo.uidFlow
            .flatMapLatest { uid ->
                // If signed out, return an empty list
                if (uid == null) repo.observeGoalsForUser("__NO_USER__")
                else repo.observeGoalsForUser(uid)
            }
            // Convert database entities to domain models
            .map { entities -> entities.map { it.toDomain() } }
            // Keep the latest list in memory for the UI
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    // Create and save a new goal
    fun addGoal(
        title: String,
        category: GoalCategory,
        deadline: Long?,
        notes: String,
        unsplashPhotoId: String? = null,
        imageThumbUrl: String? = null,
        imageRegularUrl: String? = null
    ) {
        // Don't save goals if no user is signed in
        val userId = authRepo.currentUser?.uid ?: return

        val newGoal = Goal(
            id = java.util.UUID.randomUUID().toString(),
            userId = userId,
            category = category,
            title = title.trim(),
            notes = notes,
            progress = 0,
            dateCreated = System.currentTimeMillis(),
            deadline = deadline,
            unsplashPhotoId = unsplashPhotoId,
            imageThumbUrl = imageThumbUrl,
            imageRegularUrl = imageRegularUrl
        )

        // Save to Room database
        viewModelScope.launch {
            repo.upsert(newGoal.toEntity())
        }
    }

    // Update progress and handle completed/uncompleted transitions
    fun updateGoalProgress(goalId: String, newProgress: Int) {
        val p = newProgress.coerceIn(0, 100)
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            val goal = goals.value.firstOrNull { it.id == goalId } ?: return@launch

            val wasCompleted = goal.progress >= 100
            val isCompletedNow = p >= 100

            // Set completed date only when crossing the 100% threshold
            val completedDate =
                when {
                    !wasCompleted && isCompletedNow -> now
                    wasCompleted && !isCompletedNow -> null
                    else -> goal.dateCompleted
                }

            repo.upsert(
                goal.copy(
                    progress = p,
                    dateCompleted = completedDate
                ).toEntity()
            )
        }
    }

    // Delete a goal by id
    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repo.delete(goalId)
        }
    }

    // Update goal details (title, category, notes, deadline, image)
    fun updateGoal(
        goalId: String,
        title: String,
        category: GoalCategory,
        notes: String,
        deadline: Long?,
        unsplashPhotoId: String? = null,
        imageThumbUrl: String? = null,
        imageRegularUrl: String? = null
    ) {
        viewModelScope.launch {
            val goal = goals.value.firstOrNull { it.id == goalId } ?: return@launch

            repo.upsert(
                goal.copy(
                    title = title.trim(),
                    category = category,
                    notes = notes.trim(),
                    deadline = deadline,
                    unsplashPhotoId = unsplashPhotoId,
                    imageThumbUrl = imageThumbUrl,
                    imageRegularUrl = imageRegularUrl
                ).toEntity()
            )
        }
    }
}
