package com.example.notesapp.Repository

import com.example.notesapp.Database.NotesDatabase
import com.example.notesapp.Model.Note

class NoteRepository(private val db: NotesDatabase) {

    suspend fun insertNote(note: Note) = db.getNoteDao().insertNote(note)
    suspend fun deleteNote(note: Note) = db.getNoteDao().deleteNote(note)
    suspend fun updateNote(note: Note) = db.getNoteDao().updateNote(note)

    fun getAllNotes() = db.getNoteDao().getAllNotes()
    fun searchNotes(query: String) = db.getNoteDao().searchNotes(query)
}
