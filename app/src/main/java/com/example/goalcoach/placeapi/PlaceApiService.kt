package com.example.goalcoach.placeapi

import javax.inject.Singleton
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


// Hilt module provides all dependencies related to Places and location
@Module
@InstallIn(SingletonComponent::class)
object PlacesModule {

    // Shared Moshi instance for JSON parsing
    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()


    // Retrofit API for Overpass (fetch nearby places)
    @Provides
    @Singleton
    fun provideOverpassApi(moshi: Moshi): PlacesApi =
        Retrofit.Builder()
            .baseUrl("https://overpass-api.de/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PlacesApi::class.java)


    // Retrofit API for Nominatim (reverse geocoding lat/lon to city/state)
    // User-Agent header is required by Nominatim
    @Provides
    @Singleton
    fun provideNominatimApi(moshi: Moshi): NominatimApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "GoalCoachApp/1.0")
                        .build()
                )
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NominatimApi::class.java)
    }


    // Repository for querying nearby places from Overpass
    @Provides
    @Singleton
    fun provideOverpassRepo(api: PlacesApi): OverpassRepo = OverpassRepo(api)


    // Repository for reverse geocoding coordinates into city/state
    @Provides
    @Singleton
    fun provideReverseGeocodeRepo(api: NominatimApi): ReverseGeocodeRepo = ReverseGeocodeRepo(api)


    // Repository for accessing device location
    @Provides
    @Singleton
    fun provideLocationRepo(@ApplicationContext context: Context): LocationRepo {
        val fused = LocationServices.getFusedLocationProviderClient(context)
        return LocationRepo(context, fused)
    }
}
