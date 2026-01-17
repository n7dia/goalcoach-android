package com.example.goalcoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.example.goalcoach.authentication.AuthViewModel
import com.example.goalcoach.navigation.MyNavHost
import com.example.goalcoach.navigation.NavItems
import com.example.goalcoach.scaffold.MyBottomBar
import com.example.goalcoach.scaffold.MyTopBar
import com.example.goalcoach.ui.theme.GoalCoachTheme


// Main activity that hosts the entire app UI and navigation
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Set up navigation controller
            val navController = rememberNavController()

            // AuthViewModel lives at the top level
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.state.collectAsState()

            // Observe current route
            val currentNavObject by navController.currentBackStackEntryAsState()
            val currentRoute = currentNavObject?.destination?.route

            // Redirect based on auth state
            LaunchedEffect(authState.isLoggedIn) {
                val target = if (authState.isLoggedIn) NavItems.home.path else NavItems.login.path

                // avoid re-navigating to same destination
                if (currentRoute != target) {
                    navController.navigate(target) {
                        // Clear back stack so user can't go "back" to wrong graph state
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            GoalCoachTheme {
                Scaffold(
                    // Top bar shown on all screens except login
                    topBar = {
                        if (currentRoute != NavItems.login.path) {
                            MyTopBar(
                                currentRoute = currentRoute,
                                navController = navController,
                                onLogout = { authViewModel.signOut() }
                            )
                        }
                    },
                    // Bottom bar shown only on main app screens
                    bottomBar = {
                        if (currentRoute in listOf(
                                NavItems.home.path,
                                NavItems.goals.path,
                                NavItems.journal.path,
                                NavItems.insights.path
                            )
                        ) {
                            MyBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { path ->
                                    navController.navigate(path) {
                                        // Switch tabs without growing back stack
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true  // Save screen state for restoration
                                        }
                                        // Avoid duplicate screens in stack when the same tab is repeatedly tapped
                                        launchSingleTop = true
                                        // Restore previous state when returning to a screen
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    // Host the navigation graph
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        MyNavHost(navController = navController, authViewModel = authViewModel)
                    }
                }
            }
        }
    }
}

