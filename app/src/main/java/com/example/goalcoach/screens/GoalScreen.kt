package com.example.goalcoach.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.goalcoach.scaffold.MyFAB
import com.example.goalcoach.viewmodels.GoalsViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale

@Composable
fun GoalScreen(
    viewModel: GoalsViewModel,
    onAddGoal: () -> Unit,
    onGoalClick: (String) -> Unit,
    onEditGoal: (String) -> Unit
) {
    val goals = viewModel.goals.collectAsState().value
    val pendingGoals = remember(goals) { goals.filter { !it.isCompleted } }.sortedBy { it.dateCreated }

    Scaffold(
        floatingActionButton = {
            MyFAB(onClick = onAddGoal)
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "My Goals",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (pendingGoals.isEmpty()) {
                item {
                    Text(
                        "No pending goals 🎉",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                items(
                    items = pendingGoals,
                    key = { it.id }
                ) { goal ->

                    GoalRow(
                        title = goal.title,
                        category = goal.category.key,
                        imageThumbUrl = goal.imageThumbUrl,
                        onOpen = { onGoalClick(goal.id) },
                        onEdit = { onEditGoal(goal.id) },
                        onDelete = { viewModel.deleteGoal(goal.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalRow(
    title: String,
    category: String,
    imageThumbUrl: String?,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Delete goal?") },
            text = { Text("This can’t be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showConfirmDelete = false
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image thumbnail placeholder
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 1.dp,
                modifier = Modifier.size(56.dp)
            ) {
                val context = LocalContext.current

                if (imageThumbUrl.isNullOrBlank()) {
                    Box(Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center) {
                        Text("IMG", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageThumbUrl)
                            .crossfade(true)
                            .size(112) // Decode close to 56dp*2 (safe for memory)
                            .build(),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

            }

            // Text column wrapped in a clickable area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpen() }
            ) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Overflow menu
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            showConfirmDelete = true
                        }
                    )
                }
            }
        }
    }
}