package com.example.goalcoach.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalcoach.authentication.AuthRepository
import com.example.goalcoach.models.Goal
import com.example.goalcoach.models.GoalCategory
import com.example.goalcoach.room.GoalRepository
import com.example.goalcoach.room.toDomain
import com.example.goalcoach.room.toEntity
import kotlinx.coroutines.flow.StateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repo: GoalRepository,
    private val authRepo: AuthRepository
) : ViewModel(){

    // currentUserId() as a function because the Firebase user can change while the ViewModel is alive.
    // A val would freeze the value once.
    private fun currentUserId(): String? = authRepo.currentUser?.uid

    // Goals from room database. Switch queries when userid changes.
    val goals: StateFlow<List<Goal>> =
        authRepo.uidFlow
            .flatMapLatest { uid ->
                if (uid == null) {
                    repo.observeGoalsForUser("__NO_USER__") // returns empty
                } else {
                    repo.observeGoalsForUser(uid)
                }
            }            .map { entities -> entities.map { it.toDomain() } }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )


    fun addGoal(
        title: String,
        category: GoalCategory,
        deadline: Long?,
        notes: String,
        unsplashPhotoId: String? = null,
        imageThumbUrl: String? = null,
        imageRegularUrl: String? = null
    ) {
        // return if null so you never save “unknown user” goals
        val userid = authRepo.currentUser?.uid ?: return

        val newGoal = Goal(
            id = java.util.UUID.randomUUID().toString(),
            userId = userid,
            category = category,
            title = title.trim(),
            notes = notes,
            progress = 0,
            dateCreated = System.currentTimeMillis(),
            deadline = deadline,

            // Unsplash Image
            unsplashPhotoId = unsplashPhotoId,
            imageThumbUrl = imageThumbUrl,
            imageRegularUrl = imageRegularUrl
        )

        // Add to room
        viewModelScope.launch {
            repo.upsert(newGoal.toEntity())
        }
    }

    fun updateGoalProgress(goalId: String, newProgress: Int) {
        val p = newProgress.coerceIn(0, 100)
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            val goal = goals.value.firstOrNull { it.id == goalId } ?: return@launch

            val wasCompleted = goal.progress >= 100
            val isCompletedNow = p >= 100

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

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repo.delete(goalId)
        }
    }

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

