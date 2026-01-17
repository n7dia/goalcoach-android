package com.example.goalcoach.unsplashapi

// Response returned when searching photos on Unsplash
data class UnsplashSearchResponse(
    val total: Int,                         // Total number of matching photos
    val total_pages: Int,                   // Total number of result pages
    val results: List<UnsplashPhoto>
)

// Represents a single photo from Unsplash
data class UnsplashPhoto(
    val id: String,                         // Unique photo id
    val description: String?,               // Optional description
    val alt_description: String?,           // Fallback description
    val urls: Urls                          // Image URLs in different sizes
)

// Available image URLs for a photo
data class Urls(
    val raw: String,                        // Original image
    val full: String,                       // Full-size image
    val regular: String,                    // Default display size
    val small: String,                      // Smaller image
    val thumb: String                       // Thumbnail
)