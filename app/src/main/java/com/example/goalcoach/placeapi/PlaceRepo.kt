package com.example.goalcoach.placeapi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Priority
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import com.example.goalcoach.models.NearbyPlace
import com.example.goalcoach.models.toNearbyPlaceOrNull
import com.google.android.gms.location.FusedLocationProviderClient


class OverpassRepo(private val api: PlacesApi) {

    suspend fun searchNearby(lat: Double, lon: Double, radiusMeters: Int): List<NearbyPlace> {
        val query = buildNearbyQuery(lat, lon, radiusMeters)
        val resp = api.query(query)

        return resp.elements
            .mapNotNull { it.toNearbyPlaceOrNull() }
            .distinctBy { it.osmType to it.osmId }
    }

    private fun buildNearbyQuery(lat: Double, lon: Double, radiusMeters: Int): String {
        return """
            [out:json][timeout:25];
            (
              node(around:$radiusMeters,$lat,$lon)["amenity"~"cafe|restaurant"];
              way(around:$radiusMeters,$lat,$lon)["amenity"~"cafe|restaurant"];
              relation(around:$radiusMeters,$lat,$lon)["amenity"~"cafe|restaurant"];

              node(around:$radiusMeters,$lat,$lon)["office"];
              way(around:$radiusMeters,$lat,$lon)["office"];
              relation(around:$radiusMeters,$lat,$lon)["office"];
            );
            out center tags;
        """.trimIndent()
    }
}


class LocationRepo(
    private val context: Context,
    private val fused: FusedLocationProviderClient
) {
    suspend fun getCurrentLocationOrNull(): Location? {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        return runCatching {
            fused.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).await()
        }.getOrNull()
    }

    suspend fun getLastLocationOrNull(): Location? {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        return fused.lastLocation.await()
    }

    suspend fun getFreshLocationOrNull(): Location? {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) return null

        return suspendCancellableCoroutine { cont ->
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                0L
            )
                .setMaxUpdates(1)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fused.removeLocationUpdates(this)
                    cont.resume(result.lastLocation)
                }
            }

            fused.requestLocationUpdates(request, callback, android.os.Looper.getMainLooper())

            cont.invokeOnCancellation {
                fused.removeLocationUpdates(callback)
            }
        }
    }
}

class ReverseGeocodeRepo(private val api: NominatimApi) {

    private var lastRequestTime = 0L

    suspend fun cityState(lat: Double, lon: Double): Pair<String?, String?> {
        // Nominatim requires 1 second between requests
        val now = System.currentTimeMillis()
        val timeSinceLastRequest = now - lastRequestTime
        if (timeSinceLastRequest < 1000) {
            kotlinx.coroutines.delay(1000 - timeSinceLastRequest)
        }
        lastRequestTime = System.currentTimeMillis()

        println("ReverseGeocodeRepo: Requesting city/state for lat=$lat, lon=$lon")

        val resp = api.reverse(lat = lat, lon = lon)

        println("ReverseGeocodeRepo: Full response = $resp")

        val a = resp.address

        val city = a?.city ?: a?.town ?: a?.village
        val state = a?.state

        println("ReverseGeocodeRepo: Parsed city=$city, state=$state")

        return city to state
    }
}