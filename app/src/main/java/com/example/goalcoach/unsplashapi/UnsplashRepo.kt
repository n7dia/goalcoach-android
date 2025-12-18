package com.example.goalcoach.unsplashapi

class UnsplashRepo {
    suspend fun searchPhotos(query: String, page: Int, perPage: Int = 30): UnsplashSearchResponse {
        return UnsplashApiService.unsplash_api.searchPhotos(query = query, page = page, perPage = perPage)
    }
}
