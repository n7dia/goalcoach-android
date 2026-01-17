package com.example.goalcoach.room

import javax.inject.Singleton
import android.content.Context
import androidx.room.Room
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides


// Hilt module provides database-related dependencies
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Creates and provides a single instance of the Room database
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "goalcoach.db"
        )
        // Wipes and recreates the database if the schema changes
        // Useful during development; avoid in production if data must be preserved
        .fallbackToDestructiveMigration(true)
        .build()

    // Provides DAO for goal-related database operations
    @Provides
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    // Provides DAO for journal-related database operations
    @Provides
    fun provideJournalDao(db: AppDatabase): JournalDao = db.journalDao()

    // Provides DAO for place-related database operations
    @Provides
    fun providePlaceDao(db: AppDatabase): PlaceDao = db.placeDao()
}