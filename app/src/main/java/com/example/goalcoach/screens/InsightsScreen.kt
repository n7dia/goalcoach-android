package com.example.goalcoach.screens

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goalcoach.viewmodels.ConfidenceTrendViewModel
import com.example.goalcoach.viewmodels.GoalsViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import com.example.goalcoach.viewmodels.ConfidenceSeries



@Composable
fun InsightsScreen(
    viewModel: GoalsViewModel,
    confidenceTrendViewModel: ConfidenceTrendViewModel = hiltViewModel(),
    onGoalClick: (String) -> Unit
) {
    val goals = viewModel.goals.collectAsState().value
    val trendUiState = confidenceTrendViewModel.uiState.collectAsState().value

    val completedGoals = remember(goals) {
        goals
            .filter { it.isCompleted }
            .sortedByDescending { it.dateCompleted ?: 0L }
    }

    val dateFormatter = remember {
        SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
            .apply { timeZone = java.util.TimeZone.getTimeZone("EST") }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        // Completed Goals Section
        item {
            Text(
                "Completed Goals",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (completedGoals.isEmpty()) {
            item {
                Text(
                    "No completed goals yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(
                items = completedGoals,
                key = { it.id }
            ) { goal ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
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

        // Confidence Trends Section
        item {
            Text(
                "Confidence Trends",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            if (trendUiState.isLoading) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(32.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            } else {
                ConfidenceTrendCard(series = trendUiState.series)
            }
        }



    }
}

@Composable
fun ConfidenceTrendCard(
    series: List<ConfidenceSeries>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Confidence Trends",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                series.isEmpty() -> {
                    Text(
                        text = "No confidence data available yet",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                else -> {
                    // Legend
                    Legend(series = series)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chart
                    ConfidenceChart(
                        series = series,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfidenceTrendScreen(
    viewModel: ConfidenceTrendViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confidence Trends") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.series.isEmpty() -> {
                    Text(
                        text = "No confidence data available yet",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Confidence Rating Over Time",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Legend
                        Legend(series = uiState.series)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chart
                        ConfidenceChart(
                            series = uiState.series,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Legend(series: List<ConfidenceSeries>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        series.forEach { s ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(s.color))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = s.label,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ConfidenceChart(
    series: List<ConfidenceSeries>,
    modifier: Modifier = Modifier
) {
    if (series.isEmpty()) return

    // Find min/max timestamps across all series
    val allPoints = series.flatMap { it.points }
    val minX = allPoints.minOfOrNull { it.xMs } ?: 0L
    val maxX = allPoints.maxOfOrNull { it.xMs } ?: System.currentTimeMillis()

    val minY = 0f
    val maxY = 10f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val padding = 60f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        // Calculate time range with padding for better distribution
        val timeRange = maxX - minX
        val timePadding = timeRange * 0.05f // 5% padding on each side
        val displayMinX = minX - timePadding
        val displayMaxX = maxX + timePadding

        // Draw axes
        drawLine(
            color = Color.Gray,
            start = Offset(padding, padding),
            end = Offset(padding, height - padding),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Gray,
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2f
        )

        // Helper to map data coordinates to canvas coordinates
        fun mapX(xMs: Long): Float {
            val ratio = (xMs - displayMinX).toFloat() / (displayMaxX - displayMinX).toFloat()
            return padding + ratio * chartWidth
        }

        fun mapY(y: Float): Float {
            val ratio = (y - minY) / (maxY - minY)
            return height - padding - ratio * chartHeight
        }

        // Draw grid lines and Y-axis labels (0-10)
        for (i in 0..10) {
            val y = mapY(i.toFloat())
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }

        // Draw each series
        series.forEach { s ->
            if (s.points.size < 2) {
                // Single point - just draw a circle
                if (s.points.isNotEmpty()) {
                    val pt = s.points[0]
                    drawCircle(
                        color = Color(s.color),
                        radius = 8f,
                        center = Offset(mapX(pt.xMs), mapY(pt.y))
                    )
                }
            } else {
                // Multiple points - draw line
                val path = Path()
                s.points.forEachIndexed { idx, pt ->
                    val x = mapX(pt.xMs)
                    val y = mapY(pt.y)

                    if (idx == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = Color(s.color),
                    style = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(10f, 10f),
                            phase = 0f
                        )
                    )
                )

                // Draw points
                s.points.forEach { pt ->
                    drawCircle(
                        color = Color(s.color),
                        radius = 8f,
                        center = Offset(mapX(pt.xMs), mapY(pt.y))
                    )
                }
            }
        }
    }
}