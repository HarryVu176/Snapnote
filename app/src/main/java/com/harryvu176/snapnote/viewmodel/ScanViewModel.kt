package com.harryvu176.snapnote.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.harryvu176.snapnote.data.model.Note
import com.harryvu176.snapnote.data.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository(application)
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val _scanState = MutableLiveData<ScanState>(ScanState.Initial)
    val scanState: LiveData<ScanState> = _scanState

    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> = _recognizedText

    private var currentImageUri: String? = null

    fun processImage(bitmap: Bitmap, imageUri: String) {
        currentImageUri = imageUri
        _scanState.value = ScanState.Processing

        viewModelScope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    val result = textRecognizer.process(inputImage).await()
                    result.text
                }

                if (text.isBlank()) {
                    _scanState.value = ScanState.NoTextFound
                    _recognizedText.value = ""
                } else {
                    _recognizedText.value = text
                    _scanState.value = ScanState.Success(text)
                }
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun processImageFromUri(uri: Uri) {
        currentImageUri = uri.toString()
        _scanState.value = ScanState.Processing

        viewModelScope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    val inputImage = InputImage.fromFilePath(getApplication(), uri)
                    val result = textRecognizer.process(inputImage).await()
                    result.text
                }

                if (text.isBlank()) {
                    _scanState.value = ScanState.NoTextFound
                    _recognizedText.value = ""
                } else {
                    _recognizedText.value = text
                    _scanState.value = ScanState.Success(text)
                }
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveNote(folderId: String? = null): Note {
        val text = _recognizedText.value ?: ""
        val title = generateTitle(text)

        val note = Note(
            id = UUID.randomUUID().toString(),
            title = title,
            content = text,
            imageUri = currentImageUri,
            folderId = folderId
        )

        repository.saveNote(note)
        return note
    }

    private fun generateTitle(text: String): String {
        if (text.isBlank()) return "Untitled Note"

        // Take first line or first 50 characters
        val firstLine = text.lines().firstOrNull { it.isNotBlank() } ?: text
        return if (firstLine.length > 50) {
            firstLine.take(47) + "..."
        } else {
            firstLine
        }
    }

    fun reset() {
        _scanState.value = ScanState.Initial
        _recognizedText.value = ""
        currentImageUri = null
    }

    override fun onCleared() {
        super.onCleared()
        textRecognizer.close()
    }

    sealed class ScanState {
        object Initial : ScanState()
        object Processing : ScanState()
        data class Success(val text: String) : ScanState()
        object NoTextFound : ScanState()
        data class Error(val message: String) : ScanState()
    }
}
