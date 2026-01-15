package com.example.goalcoach.placeapi

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import android.content.Context
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object PlacesModule {

    @Provides @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides @Singleton
    fun provideOverpassApi(moshi: Moshi): PlacesApi =
        Retrofit.Builder()
            .baseUrl("https://overpass-api.de/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PlacesApi::class.java)

    @Provides @Singleton
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

    @Provides @Singleton
    fun provideOverpassRepo(api: PlacesApi): OverpassRepo = OverpassRepo(api)

    @Provides @Singleton
    fun provideReverseGeocodeRepo(api: NominatimApi): ReverseGeocodeRepo = ReverseGeocodeRepo(api)

    @Provides @Singleton
    fun provideLocationRepo(@ApplicationContext context: Context): LocationRepo {
        val fused = LocationServices.getFusedLocationProviderClient(context)
        return LocationRepo(context, fused)
    }
}
