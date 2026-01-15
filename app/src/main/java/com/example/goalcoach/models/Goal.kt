package com.example.goalcoach.models


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

    // Image picked from Unsplash. Thumb for list. Regular for vision board.
    val unsplashPhotoId: String? = null,
    val imageThumbUrl: String? = null,
    val imageRegularUrl: String? = null,
){
    val isCompleted: Boolean
        get() = progress >= 100
}


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
        // Problem: The all list in the companion object is being accessed before all the GoalCategory objects are fully initialized, resulting in null values in the list.
        // Solution: use all() function
        fun all(): List<GoalCategory>  = listOf(
            Education, Mental, Physical, Nutrition, Relationships,
            Career, Finances, Spirituality, Creativity, Home
        )

        fun fromKey(key: String?): GoalCategory {
            val safeKey = key ?: return Education
            return all().firstOrNull { it.key == safeKey } ?: Education
        }
    }
}

