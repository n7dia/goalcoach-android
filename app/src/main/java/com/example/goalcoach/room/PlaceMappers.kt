package com.example.goalcoach.room

import com.example.goalcoach.models.Place


fun PlaceEntity.toDomain(): Place =
    Place(
        id = id,
        name = name,
        lat = lat,
        lon = lon,
        city = city,
        state = state,
        dateSaved = dateSaved
    )

fun Place.toEntity(userId: String): PlaceEntity =
    PlaceEntity(
        id = id,
        userId = userId,
        name = name.trim(),
        lat = lat,
        lon = lon,
        city = city,
        state = state,
        dateSaved = dateSaved
    )