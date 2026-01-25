package com.example.notesapp.Model

data class FirestoreNote(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)