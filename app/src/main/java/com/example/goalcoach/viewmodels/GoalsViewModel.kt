package com.example.goalcoach.viewmodels

import androidx.lifecycle.ViewModel
import com.example.goalcoach.models.Goal
import com.example.goalcoach.models.GoalCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


class GoalsViewModel : ViewModel(){

    private val _goals = MutableStateFlow<List<Goal>>(
        listOf(Goal("001", "nadia", GoalCategory.Education,"Learn Android", notes = "Review all lectures",0, System.currentTimeMillis(), null),
                Goal("002", "nadia", GoalCategory.Physical,"Workout for 100 hours", notes = "Biking or swimming", 0, System.currentTimeMillis(), null))
    )
    val goals : StateFlow<List<Goal>> = _goals


    fun addGoal(
        title: String,
        category: GoalCategory,
        deadline: Long?,
        notes: String,
        unsplashPhotoId: String? = null,
        imageThumbUrl: String? = null,
        imageRegularUrl: String? = null
    ) {
        val newGoal = Goal(
            id = java.util.UUID.randomUUID().toString(),
            userId = "nadia",
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
        _goals.update { it + newGoal }
    }

    fun updateGoalProgress(goalId: String, newProgress: Int) {
        val p = newProgress.coerceIn(0, 100)
        val now = System.currentTimeMillis()

        _goals.value = _goals.value.map { goal ->
            if (goal.id != goalId) return@map goal

            val wasCompleted = goal.progress >= 100
            val isCompletedNow = p >= 100

            val completedDate =
                when {
                    !wasCompleted && isCompletedNow -> now          // just completed
                    wasCompleted && !isCompletedNow -> null         // un-completed
                    else -> goal.dateCompleted                      // unchanged
                }

            goal.copy(
                progress = p,
                dateCompleted = completedDate
            )
        }
    }

    fun deleteGoal(goalId: String) {
        _goals.value = _goals.value.filterNot { it.id == goalId }

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
        _goals.value = _goals.value.map { goal ->
            if (goal.id != goalId) goal
            else goal.copy(
                title = title.trim(),
                category = category,
                notes = notes.trim(),
                deadline = deadline,
                // Unsplash Image
                unsplashPhotoId = unsplashPhotoId,
                imageThumbUrl = imageThumbUrl,
                imageRegularUrl = imageRegularUrl
            )
        }
    }

}