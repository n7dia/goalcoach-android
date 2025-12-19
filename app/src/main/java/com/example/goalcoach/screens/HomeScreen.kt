package com.example.goalcoach.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.goalcoach.authentication.AuthViewModel
import com.example.goalcoach.models.Goal
import com.example.goalcoach.viewmodels.GoalsViewModel
import androidx.compose.foundation.clickable

@Composable
fun HomeScreen(authViewModel: AuthViewModel,
               viewModel: GoalsViewModel,
               onGoalClick: (String) -> Unit) {

    // State for username
    val authState by authViewModel.state.collectAsState()
    val displayName =
        authState.userEmail
            ?.substringBefore("@")
            ?: "Guest"

    // State for pending goals list
    val goals = viewModel.goals.collectAsState().value
    val pendingGoals = remember(goals) { goals.filter { !it.isCompleted } }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Hi $displayName!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
        )

        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 1.dp,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Discipline beats motivation.",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(24.dp)
            )
        }

        VisionBoardTheme {
            VisionBoard(
            goals = pendingGoals,
            modifier = Modifier,
                onGoalClick = onGoalClick
            )
        }

    }
}

@Composable
fun VisionBoard(
    goals: List<Goal>,
    modifier: Modifier,
    onGoalClick: (String) -> Unit
) {
    // Sort goals by date of creation
    val sortedGoals = goals.sortedBy { it.dateCreated }.take(6)

    if (sortedGoals.isEmpty()) {
        // Empty state
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Add your first goal to get started!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(sortedGoals) { goal ->
                GoalCard(goal = goal,
                    Modifier.clickable{ onGoalClick(goal.id) })
            }
        }
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageUrl = goal.imageRegularUrl
    val hasImage = !imageUrl.isNullOrEmpty()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF90b4ce)) // No picture background colour
        ) {
            if (hasImage) {
                // Use AsyncImage from Coil when image is available
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .size(600) // decode-bound for a 200dp card; avoids huge bitmaps
                        .build(),
                    contentDescription = goal.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay for text readability (only when there's an image)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }

            // Text content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                // Goal title
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (hasImage) Color.White else Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Category chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = goal.category.key,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun VisionBoardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6750A4),
            primaryContainer = Color(0xFFE9DDFF),
            onPrimaryContainer = Color(0xFF22005D)
        ),
        content = content
    )
}
