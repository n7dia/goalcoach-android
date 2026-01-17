package com.example.goalcoach.unsplashapi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Provides a configured Unsplash API client
object UnsplashApiService {

    // Base URL for Unsplash API
    private const val BASE_URL = "https://api.unsplash.com/"

    // OkHttp client that adds the API key to every request
    private val okHttp = okhttp3.OkHttpClient.Builder()
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Authorization", "Client-ID ${AccessKey.UNSPLASH_ACCESS_KEY}")
                .build()
            chain.proceed(req)
        }
        .build()

    // Lazily create the Retrofit Unsplash API
    val unsplash_api: UnsplashApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UnsplashApi::class.java)
    }
}
