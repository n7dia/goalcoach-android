package com.example.goalcoach.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.goalcoach.viewmodels.GoalsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun InsightsScreen(
    viewModel: GoalsViewModel,
    onGoalClick: (String) -> Unit
) {
    val goals = viewModel.goals.collectAsState().value

    val completedGoals = remember(goals) {
        goals
            .filter { it.isCompleted }
            .sortedByDescending { it.dateCompleted ?: 0L }
    }

    val dateFormatter = remember {
        SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
            .apply { timeZone = java.util.TimeZone.getTimeZone("EST") }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Completed Goals", style = MaterialTheme.typography.headlineMedium)

        if (completedGoals.isEmpty()) {
            Text("No completed goals yet.", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    items = completedGoals,
                    key = { it.id }
                ) { goal ->

                    Card(modifier = Modifier.fillMaxWidth()){
                        Column(modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(goal.title, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(2.dp))
                            Text(goal.category.key, style = MaterialTheme.typography.bodySmall)

                            val completedText = goal.dateCompleted?.let {
                                "Completed: ${dateFormatter.format(Date(it))}"
                            } ?: "Completed: (date unknown)"

                            Text(completedText, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                }
            }
        }
    }
}