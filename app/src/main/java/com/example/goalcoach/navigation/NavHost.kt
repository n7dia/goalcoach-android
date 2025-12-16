package com.example.goalcoach.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.goalcoach.screens.AddGoalScreen
import com.example.goalcoach.screens.GoalDetailsScreen
import com.example.goalcoach.screens.GoalScreen
import com.example.goalcoach.screens.HomeScreen
import com.example.goalcoach.screens.InsightsScreen
import com.example.goalcoach.screens.JournalScreen
import com.example.goalcoach.screens.LoginScreen
import com.example.goalcoach.screens.PlacesScreen


// Navigation host (navigation graph) defines all app screens and their routes.
//      navController: controls navigation between screens
@Composable
fun MyNavHost(navController: NavHostController, modifier: Modifier = Modifier){

    // App starts at login screen
    NavHost(
        navController = navController,
        startDestination = NavItems.login.path,
        modifier = modifier
    ){
        // Login screen entry point of the app. Navigates to home onLoginSuccess.
        composable(route = NavItems.login.path) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavItems.home.path) {
                        // Clear back stack so user can't navigate back to login
                        popUpTo(NavItems.login.path) { inclusive = true }
                    }
                }
            )
        }

        // Bottom Bar Screens
        composable(route = NavItems.home.path ) { HomeScreen() }
        composable(route = NavItems.journal.path ) { JournalScreen() }
        composable(route = NavItems.insights.path ) { InsightsScreen() }

        // Goals screen includes FAB to add goals and clickable list to view details
        composable(route = NavItems.goals.path ) {
            GoalScreen(
                onAddGoal = { navController.navigate(NavItems.addGoal.path) },
                onGoalClick = { navController.navigate(NavItems.goalDetails.path) }
            )
        }

        // Detail Screens
        // Places screen accessible from Home settings menu
        composable(route = NavItems.places.path ) { PlacesScreen() }

        // Add Goal and Goal Details screens accessible from Goals screen
        composable(route = NavItems.addGoal.path ) { AddGoalScreen() }
        composable(route = NavItems.goalDetails.path ) { GoalDetailsScreen() }
    }
}

