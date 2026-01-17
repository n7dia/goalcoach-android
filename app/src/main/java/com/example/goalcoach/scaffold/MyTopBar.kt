package com.example.goalcoach.scaffold

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.example.goalcoach.navigation.NavItems


// Top app bar shown across the app
// - Shows back button on detail screens
// - Shows settings menu on the home screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(currentRoute: String?, navController: NavHostController, onLogout: () -> Unit) {
    // Show back button on detail screens (Boolean)
    val showBackButton = currentRoute in listOf(
        NavItems.places.path,
        NavItems.addGoal.path,
        NavItems.goalDetails.path
    )

    // Show settings menu only on home screen (Boolean)
    val showSettingsMenu = currentRoute == NavItems.home.path

    // Display appropriate title on current screen
    val title = NavItems.all.find { it.path == currentRoute }?.title ?: ""


    TopAppBar(
        title = { Text(title) },

        // Left Side: Back button for detail screens
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            }
        },

        // Right side: settings menu on home screen
        actions = {
            if (showSettingsMenu) {
                // Stateful boolean variable tracks whether the dropdown menu is open or closed
                var expanded by remember { mutableStateOf(false) }

                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.Settings, "Settings")
                }

                // Settings dropdown menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Navigate to Places screen
                    DropdownMenuItem(
                        text = { Text("Places") },
                        onClick = {
                            expanded = false
                            navController.navigate(NavItems.places.path)
                        }
                    )

                    // Logout and return to login screen
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            expanded = false
                            onLogout()
                        }
                    )
                }
            }
        }
    )
}