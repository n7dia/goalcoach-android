package com.example.goalcoach.navigation

import androidx.compose.ui.graphics.vector.ImageVector

// Base class for navigation items. Includes route, title, and optional icon
open class Item(val path: String, val title: String, val icon: ImageVector?)