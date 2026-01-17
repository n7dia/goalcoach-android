package com.example.goalcoach.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "places")
data class PlaceEntity(

    // Unique id for this saved place
    @PrimaryKey
    val id: String,

    // Owner of the place
    @ColumnInfo(name = "user_id")
    val userId: String,

    // Display name of the place
    @ColumnInfo(name = "name")
    val name: String,

    // Latitude of the place
    @ColumnInfo(name = "lat")
    val lat: Double,

    // Longitude of the place
    @ColumnInfo(name = "lon")
    val lon: Double,

    // City (optional)
    @ColumnInfo(name = "city")
    val city: String? = null,

    // State or province (optional)
    @ColumnInfo(name = "state")
    val state: String? = null,

    // Time the place was saved (epoch millis)
    @ColumnInfo(name = "date_saved")
    val dateSaved: Long
)
