package com.example.goalcoach.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalcoach.unsplashapi.UnsplashPhoto
import com.example.goalcoach.unsplashapi.UnsplashRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// ViewModel that searches Unsplash and exposes one photo at a time to the UI
class UnsplashViewModel : ViewModel() {

    // Data source for Unsplash API calls
    private val repo = UnsplashRepo()

    // Text the user is typing in the search box
    val queryInput = MutableStateFlow("")

    // UI state: current photo, loading flag, and error message
    val photo = MutableStateFlow<UnsplashPhoto?>(null)
    val error = MutableStateFlow<String?>(null)
    val isLoading = MutableStateFlow(false)

    // Cached search results and paging state
    private val results = mutableListOf<UnsplashPhoto>()
    private var index = 0            // current position in results list
    private var page = 1             // current page number
    private var totalPages: Int? = null
    private val perPage = 30

    init {
        // Auto-search with debounce to avoid calling the API on every keystroke
        viewModelScope.launch {
            queryInput
                .map { it.trim() }
                .debounce(600)              // wait before searching
                .distinctUntilChanged()
                .filter { it.length >= 3 }                // ignore very short queries
                .collectLatest { q ->
                    startSearch(q)
                }
        }
    }

    // Start a new search and load the first page
    private suspend fun startSearch(q: String) {
        resetState()
        loadPageAndShowFirst(q, page = 1)
    }

    // Show the next photo or fetch the next page if needed
    fun refreshNext() = viewModelScope.launch {
        val q = queryInput.value.trim()
        if (q.isBlank()) return@launch

        error.value = null

        // If we already have another photo cached, just move forward
        if (index + 1 < results.size) {
            index += 1
            photo.value = results[index]
            return@launch
        }

        // Stop if we reached the last page
        val tp = totalPages
        if (tp != null && page >= tp) {
            error.value = "No more results for '$q'."
            return@launch
        }

        // Otherwise fetch the next page and try again
        page += 1
        loadPageAppend(q, page)

        if (index + 1 < results.size) {
            index += 1
            photo.value = results[index]
        } else {
            error.value = "No more results for '$q'."
        }
    }

    // Clear cached results and reset UI state
    private fun resetState() {
        results.clear()
        index = 0
        page = 1
        totalPages = null
        photo.value = null
        error.value = null
    }

    // Load a page and display the first result
    private suspend fun loadPageAndShowFirst(q: String, page: Int) {
        try {
            isLoading.value = true

            val res = repo.searchPhotos(query = q, page = page, perPage = perPage)
            totalPages = res.total_pages

            results.clear()
            results.addAll(res.results)

            if (results.isNotEmpty()) {
                index = 0
                photo.value = results[0]
            } else {
                error.value = "No results for '$q'."
            }
        } catch (e: Exception) {
            error.value = e.message
        } finally {
            isLoading.value = false
        }
    }

    // Load a page and append results to the cache
    private suspend fun loadPageAppend(q: String, page: Int) {
        try {
            isLoading.value = true

            val res = repo.searchPhotos(query = q, page = page, perPage = perPage)
            totalPages = res.total_pages
            results.addAll(res.results)
        } catch (e: Exception) {
            error.value = e.message
        } finally {
            isLoading.value = false
        }
    }

    // Reset everything back to the initial state
    fun clearSelection() {
        photo.value = null
        error.value = null
        isLoading.value = false
        queryInput.value = ""
    }
}