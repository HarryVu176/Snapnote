package com.harryvu176.snapnote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.harryvu176.snapnote.data.model.Note
import com.harryvu176.snapnote.data.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FolderDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository(application)

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _folderName = MutableLiveData<String?>()
    val folderName: LiveData<String?> = _folderName

    private var currentFolderId: String? = null

    fun loadNotesForFolder(folderId: String?) {
        currentFolderId = folderId
        viewModelScope.launch {
            val (notesResult, folderNameResult) = fetchNotesAndFolderName(folderId)
            _notes.value = notesResult
            if (folderId == null) {
                _folderName.value = null
            } else {
                folderNameResult?.let { _folderName.value = it }
            }
        }
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
            loadNotesForFolder(currentFolderId)
        }
    }

    fun deleteNote(noteId: String) {
        repository.deleteNote(noteId)
        loadNotesForFolder(currentFolderId)
    }

    fun refresh() {
        loadNotesForFolder(currentFolderId)
    }

    private suspend fun fetchNotesAndFolderName(folderId: String?) =
        withContext(Dispatchers.IO) {
            val notesResult = repository.getNotesByFolder(folderId)
            val folderNameResult = folderId?.let { id ->
                repository.getAllFolders().find { it.id == id }?.name
            }
            notesResult to folderNameResult
        }
}
