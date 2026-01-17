package com.example.goalcoach.placeapi

import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.Manifest
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.example.goalcoach.models.NearbyPlace
import com.example.goalcoach.models.toNearbyPlaceOrNull

// Repository for querying nearby places using the Overpass API
class OverpassRepo(private val api: PlacesApi) {

    // Searches for nearby places around a latitude/longitude
    suspend fun searchNearby(lat: Double, lon: Double, radiusMeters: Int): List<NearbyPlace> {

        // Build the Overpass query string
        val query = buildNearbyQuery(lat, lon, radiusMeters)
        // Execute the query
        val resp = api.query(query)

        // Convert raw API elements into models
        // Remove duplicates by OSM type + id
        return resp.elements
            .mapNotNull { it.toNearbyPlaceOrNull() }
            .distinctBy { it.osmType to it.osmId }
    }

    // Builds the Overpass query for nearby amenities and offices
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

// Repository for accessing the device's location
class LocationRepo(
    private val context: Context,
    private val fused: FusedLocationProviderClient
) {

    // Returns the current location if permission is granted
    // Uses a balanced accuracy request (less battery usage)
    suspend fun getCurrentLocationOrNull(): Location? {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        // Abort if no location permission
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        // Safely request the current location
        return runCatching {
            fused.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).await()
        }.getOrNull()
    }

    /*
    // Returns the last known cached location (may be stale)
    suspend fun getLastLocationOrNull(): Location? {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        return fused.lastLocation.await()
    }*/

    // Actively requests a fresh location update
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

            // Start location updates
            fused.requestLocationUpdates(request, callback, android.os.Looper.getMainLooper())

            // Clean up if coroutine is cancelled
            cont.invokeOnCancellation {
                fused.removeLocationUpdates(callback)
            }
        }
    }
}

// Repository for converting latitude/longitude into city and state
class ReverseGeocodeRepo(private val api: NominatimApi) {

    // Track last request time to respect Nominatim rate limits
    private var lastRequestTime = 0L

    // Returns the city and state for a given coordinate
    suspend fun cityState(lat: Double, lon: Double): Pair<String?, String?> {

        // Nominatim requires 1 second between requests
        val now = System.currentTimeMillis()
        val timeSinceLastRequest = now - lastRequestTime
        if (timeSinceLastRequest < 1000) {
            kotlinx.coroutines.delay(1000 - timeSinceLastRequest)
        }
        lastRequestTime = System.currentTimeMillis()

        // Call the reverse geocoding API
        val resp = api.reverse(lat = lat, lon = lon)
        val address = resp.address

        // Prefer city, fall back to town or village
        val city = address?.city ?: address?.town ?: address?.village
        val state = address?.state

        return city to state
    }
}