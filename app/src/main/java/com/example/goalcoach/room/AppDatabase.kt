package com.example.goalcoach.room

import androidx.room.Database
import androidx.room.RoomDatabase

// Room database definition for the app.
// Lists all entities (tables) and the database version.
@Database(
    entities = [
        GoalEntity::class,
        JournalEntryEntity::class,
        PlaceEntity::class],
    version = 3,                        // Increment when schema changes
    exportSchema = false                // Disable schema export for simplicity
)

// Contains access objects for goal, journal, and place related database operations
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun journalDao(): JournalDao
    abstract fun placeDao(): PlaceDao
}