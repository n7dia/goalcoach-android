package com.example.goalcoach

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Application class required for Hilt.
// Initializes dependency injection when the app starts.
@HiltAndroidApp
class GoalCoach : Application()