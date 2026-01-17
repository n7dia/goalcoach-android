package com.example.goalcoach.unsplashapi

import retrofit2.http.GET
import retrofit2.http.Query

// Unsplash API for searching photos
interface UnsplashApi {

    // Search photos by keyword
    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): UnsplashSearchResponse
}