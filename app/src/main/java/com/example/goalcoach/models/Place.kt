package com.example.goalcoach.models


import com.example.goalcoach.placeapi.OverpassElement

sealed interface PlaceCandidate {
    data class Nearby(val place: NearbyPlace) : PlaceCandidate
    data class CurrentLatLon(val lat: Double, val lon: Double) : PlaceCandidate
}

data class Place(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val lat: Double,
    val lon: Double,
    val city: String? = null,
    val state: String? = null,
    val dateSaved: Long = System.currentTimeMillis()
) {
    val cityState: String
        get() = listOfNotNull(city, state).joinToString(", ").ifBlank { "Unknown" }
}

data class NearbyPlace(
    val osmType: String,
    val osmId: Long,
    val name: String,
    val lat: Double,
    val lon: Double,
    val openingHours: String?
)

data class OpenUiState(
    val untilMs: Long,
    val label: String // Open vs Closed vs Unknown
)


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

data class NominatimAddress(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val state: String? = null
)