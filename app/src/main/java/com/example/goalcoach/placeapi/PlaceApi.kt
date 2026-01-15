package com.example.goalcoach.placeapi


import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


// Overpass: What places are near me?
interface PlacesApi {
    @FormUrlEncoded
    @POST("api/interpreter")
    suspend fun query(@Field("data") query: String): PlacesResponse
}


// Nominatim: What city/state is this coordinate in?
interface NominatimApi {
    @GET("reverse")
    suspend fun reverse(
        @Query("format") format: String = "jsonv2",
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): NominatimReverseResponse
}