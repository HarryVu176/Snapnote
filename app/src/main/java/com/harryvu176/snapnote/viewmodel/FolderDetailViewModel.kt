package com.harryvu176.snapnote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.harryvu176.snapnote.data.model.Note
import com.harryvu176.snapnote.data.repository.NoteRepository

class FolderDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository(application)

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _folderName = MutableLiveData<String>()
    val folderName: LiveData<String> = _folderName

    private var currentFolderId: String? = null

    fun loadNotesForFolder(folderId: String) {
        currentFolderId = folderId
        _notes.value = repository.getNotesByFolder(folderId)

        // Load folder name
        val folder = repository.getAllFolders().find { it.id == folderId }
        folder?.let { _folderName.value = it.name }
    }

    fun renameFolder(folderId: String, newName: String) {
        val folder = repository.getAllFolders().find { it.id == folderId }
        folder?.let {
            repository.saveFolder(it.copy(name = newName))
            _folderName.value = newName
        }
    }

    fun renameNote(noteId: String, newTitle: String) {
        val note = repository.getAllNotes().find { it.id == noteId }
        note?.let {
            repository.saveNote(it.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
            currentFolderId?.let { fId -> loadNotesForFolder(fId) }
        }
    }

    fun deleteNote(noteId: String) {
        repository.deleteNote(noteId)
        currentFolderId?.let { loadNotesForFolder(it) }
    }

    fun refresh() {
        currentFolderId?.let { loadNotesForFolder(it) }
    }
}
