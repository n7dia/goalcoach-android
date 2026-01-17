package com.example.goalcoach.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.goalcoach.authentication.AuthRepository
import com.example.goalcoach.models.Place
import com.example.goalcoach.models.PlaceCandidate
import com.example.goalcoach.placeapi.LocationRepo
import com.example.goalcoach.placeapi.OverpassRepo
import com.example.goalcoach.placeapi.ReverseGeocodeRepo
import com.example.goalcoach.room.PlaceRepository


@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val overpassRepo: OverpassRepo,
    private val locationRepo: LocationRepo,
    private val geocodeRepo: ReverseGeocodeRepo,
    private val placeRepo: PlaceRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    // UI state for dialogs and loading
    val showPicker = mutableStateOf(false)
    val showNameDialog = mutableStateOf(false)
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    // Place options shown in the picker dialog
    val candidates = mutableStateOf<List<PlaceCandidate>>(emptyList())

    // Saved places for the signed-in user
    @OptIn(ExperimentalCoroutinesApi::class)
    val saved: StateFlow<List<Place>> =
        authRepo.uidFlow
            .flatMapLatest { uid ->
                // Return empty list when signed out
                if (uid == null) placeRepo.observeSavedPlacesForUser("__NO_USER__")
                else placeRepo.observeSavedPlacesForUser(uid)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    // User tapped the + button to add a place
    fun onAddClicked() {
        viewModelScope.launch {
            try {
                error.value = null
                isLoading.value = true

                // Get the user's current location
                val loc = locationRepo.getFreshLocationOrNull()
                if (loc == null) {
                    error.value = "Couldn't get your location yet. Try again."
                    return@launch
                }

                val lat = loc.latitude
                val lon = loc.longitude

                // Fetch nearby places and build picker options
                val nearby = overpassRepo.searchNearby(lat, lon, radiusMeters = 250)
                candidates.value =
                    listOf(PlaceCandidate.CurrentLatLon(lat, lon)) +
                            nearby.map { PlaceCandidate.Nearby(it) }

                showPicker.value = true
            } catch (e: Exception) {
                error.value = e.message ?: e.toString()
            } finally {
                isLoading.value = false
            }
        }
    }

    // User selected a place from the picker
    fun onCandidateSelected(candidate: PlaceCandidate) {
        when (candidate) {
            is PlaceCandidate.Nearby -> {
                // Save nearby place immediately
                savePlace(
                    name = candidate.place.name,
                    lat = candidate.place.lat,
                    lon = candidate.place.lon
                )
                showPicker.value = false
            }

            is PlaceCandidate.CurrentLatLon -> {
                // Ask user to name their current location
                showPicker.value = false
                showNameDialog.value = true
            }
        }
    }

    // Save current location with a user-provided name
    fun saveNamedCurrentLocation(name: String) {
        viewModelScope.launch {
            showNameDialog.value = false

            val loc = locationRepo.getFreshLocationOrNull()
            if (loc == null) {
                error.value = "Couldn't get your location yet. Try again."
                return@launch
            }

            savePlace(name, loc.latitude, loc.longitude)
        }
    }

    // Create and persist a place
    private fun savePlace(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            val uid = authRepo.currentUser?.uid ?: return@launch

            // Resolve city/state from coordinates
            val (city, state) = geocodeRepo.cityState(lat, lon)

            val place = Place(
                id = java.util.UUID.randomUUID().toString(),
                name = name.trim(),
                lat = lat,
                lon = lon,
                city = city,
                state = state,
                dateSaved = System.currentTimeMillis()
            )

            // Save place to Room
            placeRepo.upsert(uid, place)
        }
    }
}