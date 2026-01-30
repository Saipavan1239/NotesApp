package com.example.notesapp.Repository

import com.example.notesapp.Database.NotesDatabase
import com.example.notesapp.Database.UserProfileEntity
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileRepository(
    private val db: NotesDatabase,
    private val firestore: FirebaseFirestore
) {

    /* ---------- ROOM (IMAGE) ---------- */

    suspend fun getLocalProfile(uid: String): UserProfileEntity? {
        return db.getUserProfileDao().getProfile(uid)
    }

    suspend fun saveLocalProfile(profile: UserProfileEntity) {
        db.getUserProfileDao().insertProfile(profile)
    }

    suspend fun deleteLocalProfile(uid: String) {
        db.getUserProfileDao().deleteProfile(uid)
    }

    /* ---------- FIRESTORE (NAME + ABOUT) ---------- */

    fun getUserDetails(
        uid: String,
        onResult: (name: String, about: String) -> Unit
    ) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Your Name"

                val abouts = doc.get("abouts") as? List<String>
                val randomAbout = if (!abouts.isNullOrEmpty()) {
                    abouts.random()
                } else {
                    "Tap to add about"
                }

                onResult(name, randomAbout)
            }
    }

    fun addNewAbout(uid: String, newAbout: String) {
        val userRef = firestore.collection("users").document(uid)

        userRef.update(
            "abouts", com.google.firebase.firestore.FieldValue.arrayUnion(newAbout),
            "lastUpdated", System.currentTimeMillis()
        )
    }



    fun updateName(uid: String, name: String) {
        firestore.collection("users")
            .document(uid)
            .update("name", name)
    }

    fun updateAbout(uid: String, about: String) {
        firestore.collection("users")
            .document(uid)
            .update("about", about)
    }

    private val defaultAboutList = listOf(
        "Writing thoughts, one note at a time.",
        "Turning ideas into notes.",
        "A quiet mind writes better.",
        "Capturing thoughts before they fade.",
        "Notes are my second brain."
    )

    fun ensureUserProfileExists(uid: String) {
        val userRef = firestore.collection("users").document(uid)

        userRef.get().addOnSuccessListener { document ->


            if (!document.exists()) {
                // User document does not exist → create it
                userRef.set(
                    mapOf(
                        "name" to "Your Name",
                        "abouts" to listOf(defaultAboutList.random()),
                        "lastUpdated" to System.currentTimeMillis()
                    )
                )

            } else {
                // Document exists → check missing fields
                val updates = mutableMapOf<String, Any>()

                if (!document.contains("name")) {
                    updates["name"] = "Your Name"
                }

                if (!document.contains("abouts")) {
                    updates["abouts"] = listOf(defaultAboutList.random())
                }


                if (updates.isNotEmpty()) {
                    userRef.update(updates)
                }
            }
        }
    }

}