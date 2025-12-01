package com.harryvu176.snapnote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.harryvu176.snapnote.data.model.Note
import com.harryvu176.snapnote.data.repository.NoteRepository

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository(application)

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private var currentSearchQuery: String = ""

    init {
        loadNotes()
    }

    fun loadNotes() {
        val allNotes = repository.getAllNotes().sortedByDescending { it.updatedAt }
        _notes.value = if (currentSearchQuery.isBlank()) {
            allNotes
        } else {
            allNotes.filter {
                it.title.contains(currentSearchQuery, ignoreCase = true) ||
                it.content.contains(currentSearchQuery, ignoreCase = true)
            }
        }
    }

    fun searchNotes(query: String) {
        currentSearchQuery = query
        loadNotes()
    }

    fun renameNote(noteId: String, newTitle: String) {
        val note = repository.getAllNotes().find { it.id == noteId }
        note?.let {
            repository.saveNote(it.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
            loadNotes()
        }
    }

    fun deleteNote(noteId: String) {
        repository.deleteNote(noteId)
        loadNotes()
    }
}
