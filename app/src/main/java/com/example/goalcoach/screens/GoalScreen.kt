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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.goalcoach.scaffold.MyFAB
import com.example.goalcoach.viewmodels.GoalsViewModel


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
        containerColor = Color.White,
        floatingActionButton = {
            MyFAB(onClick = onAddGoal)
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (pendingGoals.isEmpty()) {
                item {
                    Text(
                        "No pending goals 🎉",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF8E8E93)
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
            title = { 
                Text(
                    "Delete goal?",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E)
                )
            },
            text = { 
                Text(
                    "This can't be undone.",
                    color = Color(0xFF8E8E93)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showConfirmDelete = false
                    }
                ) { 
                    Text("Delete", color = Color(0xFFFF3B30), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) { 
                    Text("Cancel", color = Color(0xFF757575))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image thumbnail placeholder
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 0.dp,
                modifier = Modifier.size(56.dp),
                color = Color(0xFFF2F2F7)
            ) {
                val context = LocalContext.current

                if (imageThumbUrl.isNullOrBlank()) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "IMG",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF8E8E93)
                        )
                    }
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageThumbUrl)
                            .crossfade(true)
                            .size(112)
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
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    category,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Medium
                )
            }

            // Overflow menu
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color(0xFF8E8E93)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFF757575)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFFF3B30)) },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFFF3B30)
                            )
                        },
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
