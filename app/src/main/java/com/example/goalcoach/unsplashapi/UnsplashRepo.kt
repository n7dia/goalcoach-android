package com.example.goalcoach.unsplashapi

// Repository for Unsplash photo data
class UnsplashRepo {

    // Search Unsplash photos by keyword
    suspend fun searchPhotos(query: String, page: Int, perPage: Int = 30): UnsplashSearchResponse {
        return UnsplashApiService.unsplash_api.searchPhotos(query = query, page = page, perPage = perPage)
    }
}
