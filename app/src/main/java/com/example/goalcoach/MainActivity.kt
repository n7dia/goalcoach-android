package com.example.goalcoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.goalcoach.navigation.MyNavHost
import com.example.goalcoach.navigation.NavItems
import com.example.goalcoach.scaffold.MyBottomBar
import com.example.goalcoach.scaffold.MyTopBar
import com.example.goalcoach.ui.theme.GoalCoachTheme


// Main activity hosts the entire app navigation and UI structure,
//      sets up the navigation controller and scaffold with top/bottom bars.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Set up navigation controller and observe current route
            val navController = rememberNavController()
            val currentNavObject by navController.currentBackStackEntryAsState()
            val currentRoute = currentNavObject?.destination?.route

            GoalCoachTheme {
                Scaffold(
                    // Hide top bar only on login screen for cleaner UI
                    topBar = {
                        if (currentRoute != NavItems.login.path) {
                            MyTopBar(
                                currentRoute = currentRoute,
                                navController = navController
                            )
                        }
                    },
                    // Show bottom bar only on main navigation screens
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
                                        // Pop up to the start destination to avoid building a large back stack
                                        // when switching between bottom bar items (clears back stack)
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
                    // Apply padding from top/bottom bars and display navigation host
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        MyNavHost(navController)
                    }
                }
            }
        }
    }
}