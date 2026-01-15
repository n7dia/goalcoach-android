package com.example.goalcoach.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "places")
data class PlaceEntity(

    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "lat")
    val lat: Double,

    @ColumnInfo(name = "lon")
    val lon: Double,

    @ColumnInfo(name = "city")
    val city: String? = null,

    @ColumnInfo(name = "state")
    val state: String? = null,

    @ColumnInfo(name = "date_saved")
    val dateSaved: Long
)
