package com.example.goalcoach.placeapi

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


// Overpass API
// Used to fetch nearby places (cafes, offices, etc.) from OpenStreetMap
interface PlacesApi {
    // Sends an Overpass query string and returns matching places
    @FormUrlEncoded
    @POST("api/interpreter")
    suspend fun query(@Field("data") query: String): PlacesResponse
}


// Nominatim API
// // Used to convert latitude/longitude into a city and state
interface NominatimApi {
    // Reverse geocodes coordinates into a readable address
    @GET("reverse")
    suspend fun reverse(
        @Query("format") format: String = "jsonv2",
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): NominatimReverseResponse
}