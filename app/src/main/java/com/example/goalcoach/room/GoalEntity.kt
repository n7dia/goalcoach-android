package com.example.goalcoach.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity (
    @PrimaryKey val id: String,
    val userId: String,
    val categoryKey: String,
    val title: String,
    val notes: String,
    val progress: Int,
    val dateCreated: Long,
    val deadline: Long?,
    val dateCompleted: Long?,

    // Optional image fields (match your Goal model)
    val unsplashPhotoId: String?,
    val imageThumbUrl: String?,
    val imageRegularUrl: String?
)
