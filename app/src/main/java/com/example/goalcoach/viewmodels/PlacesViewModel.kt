package com.example.goalcoach.viewmodels


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalcoach.authentication.AuthRepository
import kotlinx.coroutines.launch
import com.example.goalcoach.models.Place
import com.example.goalcoach.models.PlaceCandidate
import com.example.goalcoach.placeapi.LocationRepo
import com.example.goalcoach.placeapi.OverpassRepo
import com.example.goalcoach.placeapi.ReverseGeocodeRepo
import com.example.goalcoach.room.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val overpassRepo: OverpassRepo,
    private val locationRepo: LocationRepo,
    private val geocodeRepo: ReverseGeocodeRepo,
    private val placeRepo: PlaceRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    // dialog state
    val showPicker = mutableStateOf(false)
    val showNameDialog = mutableStateOf(false)
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    // candidates displayed in picker
    val candidates = mutableStateOf<List<PlaceCandidate>>(emptyList())

    // Saved places from Room - user-scoped
    @OptIn(ExperimentalCoroutinesApi::class)
    val saved: StateFlow<List<Place>> =
        authRepo.uidFlow
            .flatMapLatest { uid ->
                if (uid == null) placeRepo.observeSavedPlacesForUser("__NO_USER__")
                else placeRepo.observeSavedPlacesForUser(uid)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    fun onAddClicked() {
        viewModelScope.launch {
            try {
                error.value = null
                isLoading.value = true

                val loc = locationRepo.getFreshLocationOrNull()

                if (loc == null) {
                    error.value = "Couldn't get your location yet. Try again."
                    return@launch
                }

                val lat = loc.latitude
                val lon = loc.longitude

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

    fun onCandidateSelected(candidate: PlaceCandidate) {
        when (candidate) {
            is PlaceCandidate.Nearby -> {
                // Save immediately using the OSM name
                savePlace(
                    name = candidate.place.name,
                    lat = candidate.place.lat,
                    lon = candidate.place.lon
                )
                showPicker.value = false
            }

            is PlaceCandidate.CurrentLatLon -> {
                showPicker.value = false
                showNameDialog.value = true
            }
        }
    }

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

    private fun savePlace(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            val uid = authRepo.currentUser?.uid ?: return@launch

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

            placeRepo.upsert(uid, place)
        }
    }
}
