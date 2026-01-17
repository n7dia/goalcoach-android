package com.example.goalcoach.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "How are you feeling today?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E),
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
                    trailingIcon = { 
                        Icon(
                            Icons.Default.ArrowDropDown,
                            "dropdown",
                            tint = Color(0xFF757575)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF757575),
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    )
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
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 0.dp,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF2F2F7)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val label =
                            if (confidenceTouched) "Confidence Level: ${confidence.toInt()}" else "Confidence Level"
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )


                        TextButton(
                            onClick = { confidenceTouched = false },
                            enabled = confidenceTouched,
                            modifier = Modifier.alpha(if (confidenceTouched) 1f else 0f)
                        ) {
                            Text("Clear", color = Color(0xFF757575))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = confidence,
                            onValueChange = {
                                confidence = it
                                confidenceTouched = true
                            },
                            valueRange = 0f..10f,
                            steps = 9,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                activeTrackColor = Color(0xFF5f7f99),
                                inactiveTrackColor = Color(0xFF5f7f99),
                                activeTickColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                inactiveTickColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
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
                maxLines = 8,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF757575),
                    unfocusedBorderColor = Color(0xFFE5E5EA)
                )
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
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    "Save Entry",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
