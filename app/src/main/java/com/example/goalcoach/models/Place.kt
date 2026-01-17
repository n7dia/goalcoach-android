package com.example.goalcoach.models

import com.example.goalcoach.placeapi.OverpassElement

/**
 * Represents an item the user can choose when adding a place.
 */
sealed interface PlaceCandidate {
    // A nearby place returned from the map API
    data class Nearby(val place: NearbyPlace) : PlaceCandidate

    // The user's current latitude and longitude
    data class CurrentLatLon(val lat: Double, val lon: Double) : PlaceCandidate
}


/**
 * Represents a place saved by the user.
 */
data class Place(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val lat: Double,
    val lon: Double,
    val city: String? = null,
    val state: String? = null,
    val dateSaved: Long = System.currentTimeMillis()
) {
    // Displays city and state together, or "Unknown" if missing
    val cityState: String
        get() = listOfNotNull(city, state).joinToString(", ").ifBlank { "Unknown" }
}


/**
 * Represents a nearby place returned from OpenStreetMap
 */
data class NearbyPlace(
    val osmType: String,
    val osmId: Long,
    val name: String,
    val lat: Double,
    val lon: Double,
    val openingHours: String?
)


/**
 * UI state for temporarily showing whether a place is open.
 */
data class OpenUiState(
    val untilMs: Long,
    val label: String // Open vs Closed vs Unknown
)


/**
 * Converts a raw Overpass API element into a NearbyPlace.
 * Returns null if required data is missing.
 */
fun OverpassElement.toNearbyPlaceOrNull(): NearbyPlace? {
    val tagsMap = tags ?: emptyMap()

    val la = lat ?: center?.lat ?: return null
    val lo = lon ?: center?.lon ?: return null

    val displayName = tagsMap["name"]
        ?: tagsMap["brand"]
        ?: tagsMap["operator"]

    if (displayName.isNullOrBlank()) return null

    return NearbyPlace(
        osmType = type,
        osmId = id,
        name = displayName.trim(),
        lat = la,
        lon = lo,
        openingHours = tagsMap["opening_hours"]
    )
}


/**
 * Address data returned from the Nominatim reverse geocoding API.
 */
data class NominatimAddress(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val state: String? = null
)