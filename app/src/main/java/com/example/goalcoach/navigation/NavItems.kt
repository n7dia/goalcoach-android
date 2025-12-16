package com.example.goalcoach.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mode


// Sealed class defines all navigation destinations.
// Icons are only provided for bottom bar screens.
sealed class NavItems {
    object login : Item("login", "Login", null)
    object home : Item("home", "Home", Icons.Default.Home)
    object places : Item("places", "Places", null)
    object goals : Item("goals", "Goals", Icons.Default.DoneOutline)
    object addGoal : Item("addGoal", "Add Goal", null)
    object goalDetails : Item("goalDetails", "Goal Details", null)
    object journal : Item("journal", "Journal", Icons.Default.Mode)
    object insights: Item("insights", "Insights", Icons.Default.Assessment)
}