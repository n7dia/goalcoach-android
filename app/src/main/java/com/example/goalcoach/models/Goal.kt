package com.example.goalcoach.models

// Represents a single goal created by a user.
data class Goal(
    val id: String,
    val userId: String,
    val category: GoalCategory,
    val title: String,
    val notes: String,
    val progress: Int,
    val dateCreated: Long,
    val deadline: Long?,
    val dateCompleted: Long? = null,

    // Optional image selected from Unsplash
    // Image picked from Unsplash. Thumb for list. Regular for vision board.
    val unsplashPhotoId: String? = null,
    val imageThumbUrl: String? = null,
    val imageRegularUrl: String? = null,
){
    // A goal is completed when progress reaches 100%
    val isCompleted: Boolean
        get() = progress >= 100
}

// Defines the available goal categories
sealed class GoalCategory(val key: String) {
    data object Education : GoalCategory("EDUCATION")
    data object Mental : GoalCategory("MENTAL WELLBEING")
    data object Physical : GoalCategory("PHYSICAL WELLBEING")
    data object Nutrition : GoalCategory("NUTRITION")
    data object Relationships : GoalCategory("FAMILY & RELATIONSHIPS")
    data object Career : GoalCategory("CAREER")
    data object Finances : GoalCategory("FINANCES")
    data object Spirituality : GoalCategory("SPIRITUALITY")
    data object Creativity: GoalCategory("CREATIVITY")
    data object Home : GoalCategory("HOME")

    companion object {
        // Returns all available goal categories
        // (Why function: when all list is accessed before all the GoalCategory objects are fully initialized, results in null values in the list.)
        fun all(): List<GoalCategory>  = listOf(
            Education, Mental, Physical, Nutrition, Relationships,
            Career, Finances, Spirituality, Creativity, Home
        )

        // Converts a stored string key into a GoalCategory
        fun fromKey(key: String?): GoalCategory {
            val safeKey = key ?: return Education
            return all().firstOrNull { it.key == safeKey } ?: Education
        }
    }
}

