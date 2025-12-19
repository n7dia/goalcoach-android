package com.example.goalcoach.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goalcoach.models.GoalCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen() {

    // For sealed objects: save the key
    var categoryKey by rememberSaveable { mutableStateOf(GoalCategory.Education.key) }
    val category = GoalCategory.fromKey(categoryKey)

    var expanded by remember { mutableStateOf(false) }
    var confidence by remember { mutableFloatStateOf(5f) }
    var journalText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }

    val feelings = listOf("Happy", "Sad", "Anxious", "Calm", "Excited", "Tired", "Stressed", "Content")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title
        Text(
            text = "How are you feeling today?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Dropdown menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = categoryKey,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select category") },
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, "dropdown")
                },
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
                            categoryKey = cat.key
                            expanded = false
                        }
                    )
                }
            }
        }

        // Confidence slider
        Column {
            Text(
                text = "Confidence Level: $confidence",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Slider(
                    value = confidence,
                    onValueChange = { confidence = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.weight(1f)
                )

            }
        }

        // Journal text field
        OutlinedTextField(
            value = journalText,
            onValueChange = { journalText = it },
            label = { Text("Journal entry") },
            placeholder = { Text("Write your thoughts here.") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 8
        )

        // Audio recording button with visualizer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { isRecording = !isRecording },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isRecording) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Record audio",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            if (isRecording) {
                Spacer(modifier = Modifier.width(16.dp))
                AudioVisualizer()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Submit button
        Button(
            onClick = {
                // Handle submit
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Submit", fontSize = 16.sp)
        }
    }
}

@Composable
fun AudioVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "audio")

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val height by infiniteTransition.animateFloat(
                initialValue = 10f,
                targetValue = 40f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 500 + index * 100,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$index"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}