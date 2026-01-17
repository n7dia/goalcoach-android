package com.example.goalcoach.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mode


// Sealed class defines all navigation destinations.
// Icons are only provided for bottom bar screens.
// Goal details and edit goals nav items require goal id.
sealed class NavItems {

    // Auth Screen
    object login : Item("login", "Login", null)

    // Bottom navigation screens
    object home : Item("home", "Home", Icons.Default.Home)
    object goals : Item("goals", "My Goals", Icons.Default.DoneOutline)
    object journal : Item("journal", "Journal", Icons.Default.Mode)
    object insights: Item("insights", "Insights", Icons.Default.Assessment)

    // Goal detail screens
    object addGoal : Item("addGoal", "Add Goal", null)
    object goalDetails : Item("goalDetails/{goalId}", "Goal Details", null){
        fun createRoute(goalId: String) = "goalDetails/$goalId"
    }
    object editGoal : Item("editGoal/{goalId}", "Edit Goal", null) {
        fun createRoute(goalId: String) = "editGoal/$goalId"
    }

    // Places screen
    object places : Item("places", "Places", null)

    // List of all navigation items
    companion object {
        val all = listOf(login, home, places, goals, addGoal, goalDetails, editGoal, journal, insights)
    }
}