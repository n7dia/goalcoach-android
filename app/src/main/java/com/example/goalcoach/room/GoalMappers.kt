package com.example.goalcoach.room

import com.example.goalcoach.models.Goal
import com.example.goalcoach.models.GoalCategory

fun GoalEntity.toDomain(): Goal = Goal(
    id = id,
    userId = userId,
    category = GoalCategory.fromKey(categoryKey),
    title = title,
    notes = notes,
    progress = progress,
    dateCreated = dateCreated,
    deadline = deadline,
    dateCompleted = dateCompleted,
    unsplashPhotoId = unsplashPhotoId,
    imageThumbUrl = imageThumbUrl,
    imageRegularUrl = imageRegularUrl
)

fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    userId = userId,
    categoryKey = category.key,
    title = title,
    notes = notes,
    progress = progress,
    dateCreated = dateCreated,
    deadline = deadline,
    dateCompleted = dateCompleted,
    unsplashPhotoId = unsplashPhotoId,
    imageThumbUrl = imageThumbUrl,
    imageRegularUrl = imageRegularUrl
)