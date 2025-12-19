package com.example.goalcoach.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GoalsDatabase =
        Room.databaseBuilder(
            context,
            GoalsDatabase::class.java,
            "goalcoach.db"
        ).build()

    @Provides
    fun provideGoalDao(db: GoalsDatabase): GoalDao = db.goalDao()
}