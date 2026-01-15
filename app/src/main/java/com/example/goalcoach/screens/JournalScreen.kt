package com.example.goalcoach.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goalcoach.viewmodels.GoalsViewModel
import com.example.goalcoach.viewmodels.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: GoalsViewModel,
    journalViewModel: JournalViewModel
) {
    val goals by viewModel.goals.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var selectedGoalId by rememberSaveable { mutableStateOf<String?>(null) }

    val goalFieldText = remember(goals, selectedGoalId) {
        goals.firstOrNull { it.id == selectedGoalId }?.title ?: ""
    }

    var confidence by remember { mutableFloatStateOf(5f) }
    var confidenceTouched by rememberSaveable { mutableStateOf(false) }

    var journalText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "How are you feeling today?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Goals dropdown (blank default)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = goalFieldText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select goal") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {

                if (goals.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No goals yet") },
                        onClick = { expanded = false },
                        enabled = false
                    )
                } else {
                    goals.forEach { goal ->
                        DropdownMenuItem(
                            text = { Text(goal.title) },
                            onClick = {
                                selectedGoalId = goal.id
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Confidence slider (optional)
        Column {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val label =
                    if (confidenceTouched) "Confidence Level: ${confidence.toInt()}" else "Confidence Level"
                Text(text = label, style = MaterialTheme.typography.titleMedium)


                TextButton(
                    onClick = { confidenceTouched = false },
                    enabled = confidenceTouched,
                    modifier = Modifier.alpha(if (confidenceTouched) 1f else 0f)
                ) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = confidence,
                    onValueChange = {
                        confidence = it
                        confidenceTouched = true
                    },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.weight(1f)
                )
            }


        }

        OutlinedTextField(
            value = journalText,
            onValueChange = { journalText = it },
            label = { Text("Journal entry") },
            placeholder = { Text("Write your thoughts here.") },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            maxLines = 8
        )

        Button(
            onClick = {
                val confidenceToSave = if (confidenceTouched) confidence.toInt() else null
                journalViewModel.saveEntry(
                    goalId = selectedGoalId, // null = blank/default
                    entry = journalText,
                    confidence = confidenceToSave
                )

                // optional: clear after save
                journalText = ""
                selectedGoalId = null
                confidenceTouched = false
                confidence = 5f
            },
            enabled = journalText.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Save Entry", fontSize = 16.sp)
        }
    }
}