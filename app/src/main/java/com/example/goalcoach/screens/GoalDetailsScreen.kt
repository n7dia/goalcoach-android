package com.example.goalcoach.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.goalcoach.viewmodels.GoalsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun GoalDetailsScreen(
    viewModel: GoalsViewModel,
    goalId: String
) {
    val goals = viewModel.goals.collectAsState().value
    val goal = goals.firstOrNull { it.id == goalId }

    // Celebratory snackbar
    val snackBarHostState = remember { SnackbarHostState() }

    var wasCompleted by rememberSaveable(goalId) {
        mutableStateOf(goal?.isCompleted ?: false)
    }

    LaunchedEffect(goal?.progress) {
        val isNowCompleted = goal?.progress?.let { it >= 100 } ?: false

        if (!wasCompleted && isNowCompleted) {
            snackBarHostState.showSnackbar(
                message = "🎉 Goal completed!",
                duration = SnackbarDuration.Short
            )
        }

        wasCompleted = isNowCompleted
    }

    // Scaffold for snackbar
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {

        if (goal == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No goal selected")
            }

        }else{
            val now = System.currentTimeMillis()
            val elapsedDays = max(0, ((now - goal.dateCreated) / (24L * 60 * 60 * 1000)).toInt())

            val dateFormatter = simpleDateFormatter()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Header: image + category + title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Image placeholder (top-left)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 1.dp,
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val context = LocalContext.current

                            if (goal.imageThumbUrl.isNullOrBlank()) {
                                Box(Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center) {
                                    Text("IMG", style = MaterialTheme.typography.labelLarge)
                                }
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(goal.imageThumbUrl)
                                        .crossfade(true)
                                        .size(112) // Decode close to 56dp*2 (safe for memory)
                                        .build(),
                                    contentDescription = goal.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = goal.category.key,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                // Elapsed days + Deadline
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Elapsed: $elapsedDays day${if (elapsedDays == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    goal.deadline?.let { deadlineMillis ->
                        Text(
                            text = "Deadline: ${dateFormatter.format(Date(deadlineMillis))}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } ?: Text(
                        text = "No deadline",
                        style = MaterialTheme.typography.bodyLarge
                    )

                }

                // Progress section
                ProgressSection(
                    progress = goal.progress,
                    onProgressChangeFinished = { newProgress ->
                        viewModel.updateGoalProgress(goal.id, newProgress)
                    }
                )

                // Notes only if not blank
                if (goal.notes.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Notes", style = MaterialTheme.typography.titleMedium)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = goal.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun simpleDateFormatter(): SimpleDateFormat {
    return SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressSection(
    progress: Int,
    onProgressChangeFinished: (Int) -> Unit
) {
    var sliderValue by remember(progress) { mutableFloatStateOf(progress.toFloat()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Progress", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Text("${sliderValue.toInt()}%", style = MaterialTheme.typography.bodyMedium)
        }

        // Stack progress bar and slider together
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            // Progress bar as background
            LinearProgressIndicator(
                progress = { sliderValue / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(12.dp)
                    .align(Alignment.Center),
            )

            // Slider overlaid on top with transparent track
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 0f..100f,
                onValueChangeFinished = {
                    onProgressChangeFinished(sliderValue.toInt())
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                colors = SliderDefaults.colors(
                    // Make the track transparent
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                )
            )
        }
    }
}