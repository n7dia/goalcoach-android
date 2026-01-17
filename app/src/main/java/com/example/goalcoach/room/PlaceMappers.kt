package com.example.goalcoach.room

import com.example.goalcoach.models.Place

// Convert Place database entity to Place domain model
fun PlaceEntity.toDomain(): Place = Place(
        id = id,
        name = name,
        lat = lat,
        lon = lon,
        city = city,
        state = state,
        dateSaved = dateSaved
)

// Convert Place domain model to Place database entity
// userId is passed in since Place does not store it directly
fun Place.toEntity(userId: String): PlaceEntity = PlaceEntity(
        id = id,
        userId = userId,
        name = name.trim(),
        lat = lat,
        lon = lon,
        city = city,
        state = state,
        dateSaved = dateSaved
)