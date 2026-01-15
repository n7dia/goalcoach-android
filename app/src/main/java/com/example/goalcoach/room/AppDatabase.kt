package com.example.goalcoach.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GoalEntity::class, JournalEntryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun journalDao(): JournalDao
}