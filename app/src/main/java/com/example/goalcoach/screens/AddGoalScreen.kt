package com.example.goalcoach.screens

import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.compose.foundation.background
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.goalcoach.models.GoalCategory
import com.example.goalcoach.viewmodels.UnsplashViewModel
import com.example.goalcoach.viewmodels.GoalsViewModel


// Screen used for both creating a new goal and editing an existing goal
@Composable
fun AddGoalScreen(
    viewModel: GoalsViewModel,
    unsplashViewModel: UnsplashViewModel,
    goalId: String? = null,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    // Load existing goal when editing
    val goals = viewModel.goals.collectAsState().value
    val existingGoal = remember(goals, goalId) { goals.firstOrNull { it.id == goalId } }
    val isEditMode = goalId != null

    // Form fields
    var title by rememberSaveable { mutableStateOf("") }
    var categoryKey by rememberSaveable { mutableStateOf(GoalCategory.Education.key) }
    val category = GoalCategory.fromKey(categoryKey)

    // Deadline state
    var deadlineMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Store selected image
    var selectedPhotoId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedThumbUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedRegularUrl by rememberSaveable { mutableStateOf<String?>(null) }

    // Notes
    var notes by rememberSaveable { mutableStateOf("") }

    // Prefill fields when editing
    LaunchedEffect(existingGoal?.id) {
        existingGoal?.let { g ->
            title = g.title
            categoryKey = g.category.key
            deadlineMillis = g.deadline
            notes = g.notes
            selectedPhotoId = g.unsplashPhotoId
            selectedThumbUrl = g.imageThumbUrl
            selectedRegularUrl = g.imageRegularUrl
        }
    }

    // Basic validation
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
        UnsplashPhotoSection(
            viewModel = unsplashViewModel,
            initialPhotoId = existingGoal?.unsplashPhotoId,
            initialThumbUrl = existingGoal?.imageThumbUrl,
            initialRegularUrl = existingGoal?.imageRegularUrl,

            selectedPhotoId = selectedPhotoId,
            selectedThumbUrl = selectedThumbUrl,
            selectedRegularUrl = selectedRegularUrl,

            onAutoSelect = { photoId, thumbUrl, regularUrl ->
                selectedPhotoId = photoId
                selectedThumbUrl = thumbUrl
                selectedRegularUrl = regularUrl
            }
        )


        // Selected image confirmation
        selectedPhotoId?.let {
            Text(
                text = "Selected image: ${selectedPhotoId}",
                style = MaterialTheme.typography.labelSmall
            )
        } ?: Text("")

        // Notes input
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            maxLines = 6
        )

        // Show warning if editing a goal that no longer exists
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

                    // Update existing goal or create a new one
                    if (isEditMode && existingGoal != null) {
                        viewModel.updateGoal(
                            goalId = existingGoal.id,
                            title = trimmedTitle,
                            category = category,
                            notes = trimmedNotes,
                            deadline = deadlineMillis,
                            unsplashPhotoId = selectedPhotoId,
                            imageThumbUrl = selectedThumbUrl,
                            imageRegularUrl = selectedRegularUrl
                        )
                    } else {
                        viewModel.addGoal(
                            title = trimmedTitle,
                            category = category,
                            notes = trimmedNotes,
                            deadline = deadlineMillis,
                            unsplashPhotoId = selectedPhotoId,
                            imageThumbUrl = selectedThumbUrl,
                            imageRegularUrl = selectedRegularUrl
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
    // Format deadline for display
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
fun UnsplashPhotoSection(
    viewModel: UnsplashViewModel,
    initialPhotoId: String? = null,
    initialThumbUrl: String? = null,
    initialRegularUrl: String? = null,

    selectedPhotoId: String?,
    selectedThumbUrl: String?,
    selectedRegularUrl: String?,

    onAutoSelect: (photoId: String?, thumbUrl: String?, regularUrl: String?) -> Unit
) {
    val query by viewModel.queryInput.collectAsState()
    val photo by viewModel.photo.collectAsState()
    val error by viewModel.error.collectAsState()
    val loading by viewModel.isLoading.collectAsState()

    // Prefill for edit mode (only if Unsplash VM has no photo yet)
    LaunchedEffect(initialPhotoId, initialThumbUrl, initialRegularUrl) {
        val hasSelection = !selectedThumbUrl.isNullOrBlank() || !selectedRegularUrl.isNullOrBlank() || !selectedPhotoId.isNullOrBlank()
        if (!hasSelection && (initialThumbUrl != null || initialRegularUrl != null || initialPhotoId != null)) {
            onAutoSelect(initialPhotoId, initialThumbUrl, initialRegularUrl)
        }
    }


    // Auto-select whenever the displayed photo changes (after search/refresh)
    LaunchedEffect(photo?.id) {
        photo?.let { p ->
            onAutoSelect(p.id, p.urls.thumb, p.urls.regular)
        }
    }

    Text("Image", style = MaterialTheme.typography.titleMedium)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.queryInput.value = it },
            label = { Text("Search term") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        // Refresh
        IconButton(
            onClick = { viewModel.refreshNext() },
            enabled = query.trim().length >= 2 && !loading
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Next image")
        }

        // Clear selection (works even if there is no current photo)
        IconButton(
            onClick = {
                viewModel.clearSelection()
                onAutoSelect(null, null, null)
                      },
            enabled = !loading
        ) {
            Icon(Icons.Default.Close, contentDescription = "Clear image")
        }
    }

    // Decide what image to show:
    // 1) Current Unsplash result photo
    // 2) Initial image in edit mode if no current photo
    val previewUrl = selectedRegularUrl ?: selectedThumbUrl

    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Image fills container
            if (!previewUrl.isNullOrBlank()) {
                AsyncImage(
                    model = previewUrl,
                    contentDescription = photo?.description ?: photo?.alt_description ?: "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // One status message at a time (no overlaps)
            when {
                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                    }
                }

                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        OverlayMessage("Error: $error")
                    }
                }

                // "No results" case: query entered but photo is null
                query.trim().length >= 2 && photo == null && previewUrl.isNullOrBlank() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        OverlayMessage("No results. Try another search term.")
                    }
                }

                // Blank query + no preview image
                query.isBlank() && previewUrl.isNullOrBlank() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        OverlayMessage("Enter a term to preview an image")
                    }
                }
            }
        }
    }
}


@Composable
private fun OverlayMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.70f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = Bold)
    }
}
