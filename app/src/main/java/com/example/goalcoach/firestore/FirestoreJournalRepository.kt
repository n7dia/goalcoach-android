package com.example.goalcoach.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.example.goalcoach.room.JournalEntryEntity


@Singleton
class FirestoreJournalRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // Save or update a journal entry in Firestore
    suspend fun upsertEntry(userId: String, entry: JournalEntryEntity) {
        try {
            firestore
                .collection("users")
                .document(userId)
                .collection("journal_entries")
                .document(entry.id)
                .set(entry, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Delete a journal entry from Firestore
    suspend fun deleteEntry(userId: String, entryId: String) {
        try {
            firestore
                .collection("users")
                .document(userId)
                .collection("journal_entries")
                .document(entryId)
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fetch all journal entries for a user from Firestore
    suspend fun fetchEntries(userId: String): List<JournalEntryEntity> {
        return try {
            firestore
                .collection("users")
                .document(userId)
                .collection("journal_entries")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(JournalEntryEntity::class.java)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}