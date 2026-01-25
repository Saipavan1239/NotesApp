package com.example.notesapp.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.notesapp.Model.Note
import com.example.notesapp.Repository.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(app:Application, private val noteRepository: NoteRepository) : AndroidViewModel(app) {

    fun addNote(note: Note){
        viewModelScope.launch {
            noteRepository.insertNote(note)
        }
    }

    fun deleteNode(note: Note){
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    fun updateNode(note: Note){
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }

    fun getAllNotes() = noteRepository.getAllNotes()

    fun searchNotes(query: String?): LiveData<List<Note>> {
        return noteRepository.searchNotes(query ?: "")
    }


}