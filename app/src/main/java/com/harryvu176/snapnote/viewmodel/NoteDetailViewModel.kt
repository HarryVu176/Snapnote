package com.harryvu176.snapnote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.harryvu176.snapnote.data.model.Note
import com.harryvu176.snapnote.data.repository.NoteRepository

sealed class TranslationState {
    object Idle : TranslationState()
    object Loading : TranslationState()
    data class Success(val translation: String) : TranslationState()
    data class Error(val message: String) : TranslationState()
}

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository(application)

    private val _note = MutableLiveData<Note?>()
    val note: LiveData<Note?> = _note

    private val _isEditMode = MutableLiveData(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _translationState = MutableLiveData<TranslationState>(TranslationState.Idle)
    val translationState: LiveData<TranslationState> = _translationState

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

    fun translateNote() {
        val currentNote = _note.value ?: return
        val content = currentNote.content

        if (content.isBlank()) {
            _translationState.value = TranslationState.Error("Note content is empty")
            return
        }

        _translationState.value = TranslationState.Loading

        // Configure translation: Auto-detect (or assume English for now) to French (or preferred language)
        // For simplicity, let's translate from English to French. 
        // In a real app, you might detect language or let user pick.
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.FRENCH)
            .build()
            
        val translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        // Ensure model is downloaded before translating
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(content)
                    .addOnSuccessListener { translatedText ->
                        val updatedNote = currentNote.copy(
                            translation = translatedText,
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.saveNote(updatedNote)
                        _note.value = updatedNote
                        _translationState.value = TranslationState.Success(translatedText)
                        translator.close()
                    }
                    .addOnFailureListener { e ->
                        _translationState.value = TranslationState.Error("Translation failed: ${e.localizedMessage}")
                        translator.close()
                    }
            }
            .addOnFailureListener { e ->
                _translationState.value = TranslationState.Error("Model download failed: ${e.localizedMessage}")
                translator.close()
            }
    }
}
