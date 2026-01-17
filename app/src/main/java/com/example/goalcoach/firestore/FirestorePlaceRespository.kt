package com.example.goalcoach.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.example.goalcoach.room.PlaceEntity

@Singleton
class FirestorePlaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // Save or update a place in Firestore
    suspend fun upsertPlace(userId: String, place: PlaceEntity) {
        try {
            firestore
                .collection("users")
                .document(userId)
                .collection("places")
                .document(place.id)
                .set(place, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Delete a place from Firestore
    suspend fun deletePlace(userId: String, placeId: String) {
        try {
            firestore
                .collection("users")
                .document(userId)
                .collection("places")
                .document(placeId)
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Delete all places for a user from Firestore
    suspend fun deleteAllPlaces(userId: String) {
        try {
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("places")
                .get()
                .await()

            // Batch delete all places
            snapshot.documents.forEach { doc ->
                doc.reference.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fetch all places for a user from Firestore
    suspend fun fetchPlaces(userId: String): List<PlaceEntity> {
        return try {
            firestore
                .collection("users")
                .document(userId)
                .collection("places")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(PlaceEntity::class.java)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}