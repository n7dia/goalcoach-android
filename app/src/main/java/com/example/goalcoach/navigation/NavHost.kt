package com.example.goalcoach.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.goalcoach.authentication.AuthViewModel
import com.example.goalcoach.screens.AddGoalScreen
import com.example.goalcoach.screens.GoalDetailsScreen
import com.example.goalcoach.screens.GoalScreen
import com.example.goalcoach.screens.HomeScreen
import com.example.goalcoach.screens.InsightsScreen
import com.example.goalcoach.screens.JournalScreen
import com.example.goalcoach.screens.LoginScreen
import com.example.goalcoach.screens.PlacesScreen
import com.example.goalcoach.unsplashapi.UnsplashViewModel
import com.example.goalcoach.viewmodels.GoalsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goalcoach.viewmodels.JournalViewModel


// Navigation host (navigation graph) defines all app screens and their routes.
//      navController: controls navigation between screens
@Composable
fun MyNavHost(navController: NavHostController, authViewModel: AuthViewModel, modifier: Modifier = Modifier){

    // Create one instance of GoalsViewModel shared by goals-related screens
    val goalsViewModel: GoalsViewModel = hiltViewModel()

    // App starts at login screen
    NavHost(
        navController = navController,
        startDestination = NavItems.login.path,
        modifier = modifier
    ){
        // Login screen entry point of the app. Navigates to home.
        composable(route = NavItems.login.path) {
            LoginScreen(vm = authViewModel)
        }

        // BOTTOM BAR SCREENS
        // HOME SCREEN NAVIGATION.
        composable(route = NavItems.home.path ) {
            HomeScreen(
                authViewModel = authViewModel,
                viewModel = goalsViewModel,
                onGoalClick = { goalId ->
                    navController.navigate(
                        NavItems.goalDetails.createRoute(goalId)
                    ) }
            ) }
        composable(route = NavItems.journal.path ) {
            val journalViewModel: JournalViewModel = hiltViewModel()

            JournalScreen(
                viewModel = goalsViewModel,
                journalViewModel = journalViewModel
            )
        }
        composable(route = NavItems.insights.path ) {
            InsightsScreen(
                viewModel = goalsViewModel,
                onGoalClick = { goalId ->
                    navController.navigate(NavItems.goalDetails.createRoute(goalId))}
            )
        }

        // GOAL SCREEN NAVIGATION. Includes FAB and clickable list.
        composable(route = NavItems.goals.path ) {
            GoalScreen(
                viewModel = goalsViewModel,
                onAddGoal = { navController.navigate(NavItems.addGoal.path) },
                onGoalClick = { goalId ->
                    navController.navigate(
                        NavItems.goalDetails.createRoute(goalId)
                    ) },
                onEditGoal = { goalId ->
                    navController.navigate(
                        NavItems.editGoal.createRoute(goalId)
                    )
                }
            )
        }

        // DETAIL SCREENS
        // Places screen accessible from Home settings menu
        composable(route = NavItems.places.path ) { PlacesScreen() }

        // ADD GOAL NAVIGATION
        composable(route = NavItems.addGoal.path ) {backStackEntry ->

            val unsplashVm: UnsplashViewModel = viewModel(backStackEntry)

            AddGoalScreen(
            viewModel = goalsViewModel,
            unsplashViewModel = unsplashVm,
            onDone = { navController.navigateUp() },
            onCancel = { navController.navigateUp() }
        ) }

        // GOAL DETAIL NAVIGATION
        composable(route = NavItems.goalDetails.path ) {
                backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId")
            GoalDetailsScreen(
                viewModel = goalsViewModel,
                goalId = goalId ?: ""
            )
        }

        // EDIT GOAL NAVIGATION
        composable(route = NavItems.editGoal.path) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId") ?: return@composable
            val unsplashVm: UnsplashViewModel = viewModel(backStackEntry)

            AddGoalScreen(
                viewModel = goalsViewModel,
                unsplashViewModel = unsplashVm,
                goalId = goalId,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}

