package com.example.goalcoach.room

import com.example.goalcoach.models.Goal
import com.example.goalcoach.models.GoalCategory

// Convert a database GoalEntity into a domain Goal model
fun GoalEntity.toDomain(): Goal = Goal(
    id = id,
    userId = userId,
    category = GoalCategory.fromKey(categoryKey),       // map stored key to enum
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

// Convert a domain Goal model into a GoalEntity for storage
fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    userId = userId,
    categoryKey = category.key,                         // store enum as string
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