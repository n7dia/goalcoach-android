package com.example.goalcoach.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.goalcoach.viewmodels.GoalsViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.goalcoach.models.GoalCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Add goal screen also using for editing an existing goal
@Composable
fun AddGoalScreen(
    viewModel: GoalsViewModel,
    goalId: String? = null,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    // Read current goals so we can prefill when editing
    val goals = viewModel.goals.collectAsState().value
    val existingGoal = remember(goals, goalId) { goals.firstOrNull { it.id == goalId } }
    val isEditMode = goalId != null

    // Use rememberSaveable only for primitives/Strings
    var title by rememberSaveable { mutableStateOf("") }

    // For sealed objects: save the key
    var categoryKey by rememberSaveable { mutableStateOf(GoalCategory.Education.key) }
    val category = GoalCategory.fromKey(categoryKey)

    // Deadline state
    var deadlineMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Image selector UI
    var imageQuery by rememberSaveable { mutableStateOf("") }
    var imageIndex by rememberSaveable { mutableIntStateOf(0) } // increments on refresh

    // Notes
    var notes by rememberSaveable { mutableStateOf("") }

    // Prefill when editing
    LaunchedEffect(existingGoal?.id) {
        existingGoal?.let { g ->
            title = g.title
            categoryKey = g.category.key
            deadlineMillis = g.deadline
            notes = g.notes
            // Image fields for later
            // imageQuery = g.imageQuery
            // imageIndex = g.imageIndex
        }
    }

    val canSave = title.trim().isNotEmpty()
    val canUpdate = !isEditMode || existingGoal != null // if editing, require that goal still exists

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Category dropdown
        CategoryDropdown(
            selectedKey = categoryKey,
            onSelectedKey = { categoryKey = it }
        )

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        // Deadline
        DeadlineRow(
            deadlineMillis = deadlineMillis,
            onPickDeadline = { showDatePicker = true },
            onClear = { deadlineMillis = null }
        )

        // Date Picker
        if (showDatePicker) {
            val initialPickerMillis = deadlineMillis ?: System.currentTimeMillis()

            GoalDatePicker(
                initialSelectedMillis = initialPickerMillis,
                onDismiss = { showDatePicker = false },
                onConfirm = { selectedMillis ->
                    deadlineMillis = selectedMillis
                    showDatePicker = false
                }
            )
        }

        // Image selector: search + preview
        ImageSelector(
            query = imageQuery,
            onQueryChange = { imageQuery = it },
            imageIndex = imageIndex,
            onRefresh = { imageIndex++ }
        )

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            maxLines = 6
        )

        // If editing but the goal no longer exists, show a hint
        if (isEditMode && existingGoal == null) {
            Text(
                text = "This goal no longer exists.",
                color = MaterialTheme.colorScheme.error
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) { Text("Cancel") }

            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val trimmedNotes = notes.trim()

                    if (isEditMode && existingGoal != null) {
                        viewModel.updateGoal(
                            goalId = existingGoal.id,
                            title = trimmedTitle,
                            category = category,
                            notes = trimmedNotes,
                            deadline = deadlineMillis
                        )
                    } else {
                        viewModel.addGoal(
                            title = trimmedTitle,
                            category = category,
                            notes = trimmedNotes,
                            deadline = deadlineMillis
                        )
                    }

                    onDone()
                },
                enabled = canSave && canUpdate,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selectedKey: String,
    onSelectedKey: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = remember(selectedKey) { GoalCategory.fromKey(selectedKey) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.key,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            GoalCategory.all().forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.key) },
                    onClick = {
                        onSelectedKey(cat.key)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
private fun DeadlineRow(
    deadlineMillis: Long?,
    onPickDeadline: () -> Unit,
    onClear: () -> Unit
) {
    val formatted = remember(deadlineMillis) {
        deadlineMillis?.let { millis ->
            SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .format(Date(millis))
        } ?: "No deadline"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = onPickDeadline,
            label = { Text(formatted) },
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
        )

        Spacer(Modifier.weight(1f))

        if (deadlineMillis != null) {
            TextButton(onClick = onClear) { Text("Clear") }
        }
    }
}

@Composable
private fun GoalDatePicker(
    initialSelectedMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.selectedDateMillis) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = state)
    }
}


@Composable
private fun ImageSelector(
    query: String,
    onQueryChange: (String) -> Unit,
    imageIndex: Int,
    onRefresh: () -> Unit
) {
    Text("Image", style = MaterialTheme.typography.titleMedium)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search term") },
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onRefresh,
            enabled = query.isNotBlank()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh image")
        }
    }

    // Preview placeholder
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val text = if (query.isBlank()) {
                "Enter a term to preview an image"
            } else {
                "Image preview placeholder\nQuery: \"$query\"\nResult index: $imageIndex"
            }
            Text(text)
        }
    }
}