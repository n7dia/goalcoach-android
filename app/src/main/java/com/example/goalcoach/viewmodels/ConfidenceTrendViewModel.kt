package com.example.goalcoach.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.example.goalcoach.authentication.AuthRepository
import com.example.goalcoach.room.JournalRepository
import com.example.goalcoach.room.GoalRepository
import com.example.goalcoach.room.toDomain
import javax.inject.Inject

data class ConfidencePoint(val xMs: Long, val y: Float){}

data class ConfidenceSeries(
    val key: String,          // goalId or "GENERAL"
    val label: String,        // goal title or "General"
    val points: List<ConfidencePoint>,
    val color: Long          // Color for the line
){}

data class ConfidenceTrendUiState(
    val series: List<ConfidenceSeries> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ConfidenceTrendViewModel @Inject constructor(
    private val journalRepo: JournalRepository,
    private val goalRepo: GoalRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfidenceTrendUiState())
    val uiState: StateFlow<ConfidenceTrendUiState> = _uiState.asStateFlow()

    init {
        loadTrendData()
    }

    private fun loadTrendData() {
        // Get entries from the last 90 days
        val fromMs = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)

        viewModelScope.launch {
            authRepo.uidFlow.collect { uid ->
                if (uid == null) {
                    _uiState.value = ConfidenceTrendUiState(
                        series = emptyList(),
                        isLoading = false
                    )
                    return@collect
                }

                combine(
                    journalRepo.observeConfidenceEntriesSince(uid, fromMs),
                    goalRepo.observeGoalsForUser(uid)
                ) { entries, goalEntities ->
                    // Create a map of goalId -> goal title directly from entities
                    val goalTitles = goalEntities.associate { it.id to it.title }

                    // Group entries by goalId (null becomes "GENERAL")
                    val grouped = entries.groupBy { it.goalId ?: "GENERAL" }

                    // Create series for each group
                    val seriesList = grouped.map { (key, entryList) ->
                        val label = if (key == "GENERAL") {
                            "General"
                        } else {
                            goalTitles[key] ?: "Unknown Goal"
                        }

                        val points = entryList
                            .filter { it.confidence != null }
                            .sortedBy { it.dateSubmitted }
                            .map { entry ->
                                ConfidencePoint(
                                    xMs = entry.dateSubmitted,
                                    y = entry.confidence!!.toFloat()
                                )
                            }

                        ConfidenceSeries(
                            key = key,
                            label = label,
                            points = points,
                            color = getColorForKey(key)
                        )
                    }
                        .filter { it.points.isNotEmpty() } // Only include series with data
                        .sortedBy { it.label }

                    ConfidenceTrendUiState(
                        series = seriesList,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            }
        }
    }

    private fun getColorForKey(key: String): Long {
        // Generate consistent colors for each goal
        val colors = listOf(
            0xFF3a86ff, // Blue
            0xFF04a777, // Green
            0xFFef233c, // Red
            0xFFfb5607, // Orange
            0xFF8338ec, // Purple
            0xFF54cfaa, // Cyan
            0xFFffbe0b, // Yellow
            0xFF795548, // Brown
            0xFFff006e, // Pink
            0xFF046e8f  // Blue Grey
        )

        return colors[key.hashCode().absoluteValue() % colors.size]
    }

    private fun Int.absoluteValue() = if (this < 0) -this else this
}