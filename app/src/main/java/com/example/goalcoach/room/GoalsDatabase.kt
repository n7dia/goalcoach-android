package com.example.goalcoach.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GoalEntity::class], version = 1, exportSchema = false)
abstract class GoalsDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao

}