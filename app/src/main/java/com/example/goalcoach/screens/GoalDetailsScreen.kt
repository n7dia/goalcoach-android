package com.example.goalcoach.screens

import kotlin.math.max
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.goalcoach.viewmodels.GoalsViewModel



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
        containerColor = Color.White,
        snackbarHost = {
            SnackbarHost(snackBarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color(0xFF757575),
                    contentColor = Color.White,
                    actionColor = Color.White
                )
            }
        }
    ) {

        if (goal == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No goal selected",
                    color = Color(0xFF8E8E93)
                )
            }

        } else {
            val now = System.currentTimeMillis()
            val elapsedDays = max(0, ((now - goal.dateCreated) / (24L * 60 * 60 * 1000)).toInt())

            val dateFormatter = simpleDateFormatter()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Header: image + category + title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Image placeholder (top-left)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 0.dp,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(88.dp),
                        color = Color(0xFFF2F2F7)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val context = LocalContext.current

                            if (goal.imageThumbUrl.isNullOrBlank()) {
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
                                        .data(goal.imageThumbUrl)
                                        .crossfade(true)
                                        .size(112)
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = goal.category.key,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                    }
                }

                // Elapsed days + Deadline
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 0.dp,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF2F2F7)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Elapsed: $elapsedDays day${if (elapsedDays == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF1C1C1E)
                        )

                        goal.deadline?.let { deadlineMillis ->
                            Text(
                                text = "Deadline: ${dateFormatter.format(Date(deadlineMillis))}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF1C1C1E)
                            )
                        } ?: Text(
                            text = "No deadline",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF8E8E93)
                        )
                    }
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
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 0.dp,
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF2F2F7)
                        ) {
                            Text(
                                text = goal.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1C1C1E),
                                modifier = Modifier
                                    .padding(16.dp)
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

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF2F2F7)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${sliderValue.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
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
                    color = Color(0xFF757575),
                    trackColor = Color(0xFFE5E5EA),
                )

                // Slider overlaid on top with transparent track
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..100f,
                    onValueChangeFinished = {
                        onProgressChangeFinished(sliderValue.toInt())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF757575),
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }
        }
    }
}
