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
    object login : Item("login", "Login", null)
    object home : Item("home", "Home", Icons.Default.Home)
    object places : Item("places", "Places", null)
    object goals : Item("goals", "Goals", Icons.Default.DoneOutline)
    object addGoal : Item("addGoal", "Add Goal", null)
    object goalDetails : Item("goalDetails/{goalId}", "Goal Details", null){
        fun createRoute(goalId: String) = "goalDetails/$goalId"
    }
    object editGoal : Item("editGoal/{goalId}", "Edit Goal", null) {
        fun createRoute(goalId: String) = "editGoal/$goalId"
    }

    object journal : Item("journal", "Journal", Icons.Default.Mode)
    object insights: Item("insights", "Insights", Icons.Default.Assessment)

    companion object {
        val all = listOf(login, home, places, goals, addGoal, goalDetails, editGoal, journal, insights)
    }
}