package com.example.goalcoach.unsplashapi


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class UnsplashViewModel : ViewModel() {
    private val repo = UnsplashRepo()

    // Text the user is typing
    val queryInput = MutableStateFlow("")

    // UI state
    val photo = MutableStateFlow<UnsplashPhoto?>(null)
    val error = MutableStateFlow<String?>(null)
    val isLoading = MutableStateFlow(false)

    // Internal page/index state for image search results
    private val results = mutableListOf<UnsplashPhoto>()
    private var index = 0
    private var page = 1
    private var totalPages: Int? = null
    private val perPage = 30

    init {
        // Debounced auto-search
        viewModelScope.launch {
            queryInput
                .map { it.trim() }
                .debounce(600)     // wait 0.6 seconds before search
                .distinctUntilChanged()
                .filter { it.length >= 3 }      // don't search if <3 letters entered
                .collectLatest { q ->
                    startSearch(q)
                }
        }
    }

    private suspend fun startSearch(q: String) {
        resetState()
        loadPageAndShowFirst(q, page = 1)
    }

    fun refreshNext() = viewModelScope.launch {
        val q = queryInput.value.trim()
        if (q.isBlank()) return@launch

        error.value = null

        // Move to next already-fetched photo if possible
        if (index + 1 < results.size) {
            index += 1
            photo.value = results[index]
            return@launch
        }

        // Need next page
        val tp = totalPages
        if (tp != null && page >= tp) {
            error.value = "No more results for '$q'."
            return@launch
        }

        page += 1
        loadPageAppend(q, page)
        if (index + 1 < results.size) {
            index += 1
            photo.value = results[index]
        } else {
            error.value = "No more results for '$q'."
        }
    }

    private fun resetState() {
        results.clear()
        index = 0
        page = 1
        totalPages = null
        photo.value = null
        error.value = null
    }

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

    fun clearSelection() {
        photo.value = null
        error.value = null
        isLoading.value = false
        queryInput.value = ""
    }
}