package com.example.goalcoach.scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable


// Floating Action Button for the Goals Screen
//      onClick callback executed when FAB is clicked
@Composable
fun MyFAB(onClick: () -> Unit){
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = "Add")
    }
}