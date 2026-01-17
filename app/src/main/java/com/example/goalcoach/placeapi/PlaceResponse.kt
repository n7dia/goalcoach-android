package com.example.goalcoach.placeapi

import com.example.goalcoach.models.NominatimAddress

// Root response returned by the Overpass API
data class PlacesResponse(
    // List of raw OSM elements (nodes, ways, relations)
    val elements: List<OverpassElement> = emptyList()
)

// Represents a single OpenStreetMap (OSM) element
data class OverpassElement(
    // OSM element type: node, way, or relation
    val type: String,

    // Unique OpenStreetMap ID
    val id: Long,

    // Latitude (present for nodes)
    val lat: Double? = null,

    // Longitude (present for nodes)
    val lon: Double? = null,

    // Center point for ways and relations
    val center: OverpassCenter? = null,

    // Key-value metadata such as name, brand, opening_hours
    val tags: Map<String, String>? = null
)

// Represents a computed center coordinate for non-node elements
data class OverpassCenter(
    val lat: Double,
    val lon: Double
)

// Response returned by the Nominatim reverse geocoding API
data class NominatimReverseResponse(
    val address: NominatimAddress? = null
)