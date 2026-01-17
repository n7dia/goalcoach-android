package com.example.goalcoach.scaffold

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.goalcoach.navigation.NavItems


// Bottom navigation bar displaying the main app screens
// currentRoute: the current navigation route to highlight the selected item
// onNavigate: callback to handle navigation when an item is clicked
@Composable
fun MyBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    // Define the four main navigation screens shown in bottom bar
    val navItems = listOf(NavItems.home, NavItems.goals, NavItems.journal, NavItems.insights)

    NavigationBar {
        // Create a navigation item for each screen
        navItems.forEach { item ->
            NavigationBarItem(
                onClick = { onNavigate(item.path) },
                selected = currentRoute == item.path,  // Highlight active screen
                label = { Text(item.title) },
                icon = {
                    // Display icon if the item has one
                    item.icon?.let { Icon(imageVector = it, contentDescription = item.title) }
                }
            )
        }
    }
}