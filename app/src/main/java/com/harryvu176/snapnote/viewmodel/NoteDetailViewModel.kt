package com.harryvu176.snapnote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.harryvu176.snapnote.data.model.Note
import com.harryvu176.snapnote.data.repository.NoteRepository

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository(application)

    private val _note = MutableLiveData<Note?>()
    val note: LiveData<Note?> = _note

    private val _isEditMode = MutableLiveData(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun loadNote(noteId: String) {
        _note.value = repository.getAllNotes().find { it.id == noteId }
    }

    fun toggleEditMode() {
        _isEditMode.value = !(_isEditMode.value ?: false)
    }

    fun setEditMode(enabled: Boolean) {
        _isEditMode.value = enabled
    }

    fun saveNote(title: String, content: String) {
        _note.value?.let { currentNote ->
            val updatedNote = currentNote.copy(
                title = title.ifBlank { "Untitled Note" },
                content = content,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveNote(updatedNote)
            _note.value = updatedNote
            _isEditMode.value = false
            _saveSuccess.value = true
        }
    }

    fun updateContent(content: String) {
        _note.value?.let { currentNote ->
            val updatedNote = currentNote.copy(
                content = content,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveNote(updatedNote)
            _note.value = updatedNote
        }
    }
}
