package com.harryvu176.snapnote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.harryvu176.snapnote.data.model.Note
import com.harryvu176.snapnote.data.repository.NoteRepository
import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository(application)

    private val _note = MutableLiveData<Note?>()
    val note: LiveData<Note?> = _note

    private val _isEditMode = MutableLiveData(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _isTranslating = MutableLiveData(false)
    val isTranslating: LiveData<Boolean> = _isTranslating

    private val _translationError = MutableLiveData<String?>()
    val translationError: LiveData<String?> = _translationError

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
            _translationError.value = "Note content is empty"
            return
        }

        _isTranslating.value = true
        _translationError.value = null

        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(content)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    performTranslation(currentNote, content, TranslateLanguage.ENGLISH)
                } else {
                    performTranslation(currentNote, content, languageCode)
                }
            }
            .addOnFailureListener {
                performTranslation(currentNote, content, TranslateLanguage.ENGLISH)
            }
    }

    private fun performTranslation(currentNote: Note, content: String, sourceLang: String) {
        viewModelScope.launch {
            try {
                val targetLang = if (sourceLang == TranslateLanguage.ENGLISH) {
                    TranslateLanguage.FRENCH
                } else {
                    TranslateLanguage.ENGLISH
                }

                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLang)
                    .setTargetLanguage(targetLang)
                    .build()

                val translator = Translation.getClient(options)
                val conditions = DownloadConditions.Builder().build()

                translator.downloadModelIfNeeded(conditions).await()

                val lines = content.split("\n")
                val translatedLines = lines.map { line ->
                    if (line.isBlank()) ""
                    else translator.translate(line).await()
                }
                
                val finalTranslation = translatedLines.joinToString("\n")

                val updatedNote = currentNote.copy(
                    translation = finalTranslation,
                    updatedAt = System.currentTimeMillis()
                )
                repository.saveNote(updatedNote)
                _note.value = updatedNote
                
                _isTranslating.value = false
                translator.close()

            } catch (e: Exception) {
                _isTranslating.value = false
                _translationError.value = "Translation failed: ${e.localizedMessage}"
            }
        }
    }
}
