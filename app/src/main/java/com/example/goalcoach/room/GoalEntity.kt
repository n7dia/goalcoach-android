package com.example.goalcoach.room

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "goals")
data class GoalEntity (

    // Unique goal id
    @PrimaryKey val id: String,

    // Owner of the goal (Firebase user id)
    val userId: String,

    // Stored category key (mapped to GoalCategory)
    val categoryKey: String,

    // Goal title
    val title: String,

    // Optional notes/details
    val notes: String,

    // Progress percentage (0–100)
    val progress: Int,

    // When the goal was created (timestamp)
    val dateCreated: Long,

    // Optional deadline (timestamp)
    val deadline: Long?,

    // When the goal was completed (null if not completed)
    val dateCompleted: Long?,

    // Optional image fields (match goal model)
    val unsplashPhotoId: String?,
    val imageThumbUrl: String?,
    val imageRegularUrl: String?
)
