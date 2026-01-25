package com.example.notesapp.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notesapp.Model.Note


@Database(
    entities = [Note::class],
    version = 2,
    exportSchema = false
)

abstract class NotesDatabase :RoomDatabase() {

    abstract fun getNoteDao(): NoteDao

    companion object{
        @Volatile
        private var INSTANCE: NotesDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) :NotesDatabase = INSTANCE?:
        synchronized(LOCK) {
            INSTANCE?:
            createDatabase(context).also{
                INSTANCE = it
            }

        }

        private fun createDatabase(context: Context) :NotesDatabase {
            return Room.databaseBuilder(context.applicationContext, NotesDatabase::class.java, "note_db")
               .build()
        }
    }
}