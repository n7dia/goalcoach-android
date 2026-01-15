package com.example.goalcoach.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        GoalEntity::class,
        JournalEntryEntity::class,
        PlaceEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun journalDao(): JournalDao
    abstract fun placeDao(): PlaceDao
}