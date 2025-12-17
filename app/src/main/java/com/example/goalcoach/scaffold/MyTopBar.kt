package com.example.goalcoach.scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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


// Topbar displays back button on detail screens, settings menu on home screen, and appropriate titles.
//      currentRoute: the current screen to determine topbar UI
//      navController: navigation controller handles navigation actions
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(currentRoute: String?, navController: NavHostController) {
    // Show back button only when the current screen is a detail screens (Boolean)
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
        // Right Side: Settings menu on home screen with Places and Logout options
        actions = {
            if (showSettingsMenu) {
                // Stateful boolean variable tracks whether the dropdown menu is open or closed
                var expanded by remember { mutableStateOf(false) }

                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.Settings, "Settings")
                }

                // Dropdown menu: visible/hidden based on expanded variable and dismissed when it becomes false
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
                            navController.navigate(NavItems.login.path){
                                // Clear the back stack when logging out including the destination
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    )
}