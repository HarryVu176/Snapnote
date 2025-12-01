package com.harryvu176.snapnote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.harryvu176.snapnote.data.model.Folder
import com.harryvu176.snapnote.data.repository.NoteRepository
import java.util.UUID

class FoldersViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository(application)

    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>> = _folders

    private val _unsortedNotesCount = MutableLiveData<Int>()
    val unsortedNotesCount: LiveData<Int> = _unsortedNotesCount

    init {
        loadFolders()
    }

    fun loadFolders() {
        _folders.value = repository.getAllFolders()
        _unsortedNotesCount.value = repository.getUnsortedNotes().size
    }

    fun createFolder(name: String) {
        val folder = Folder(
            id = UUID.randomUUID().toString(),
            name = name
        )
        repository.saveFolder(folder)
        loadFolders()
    }

    fun renameFolder(folderId: String, newName: String) {
        val folder = repository.getAllFolders().find { it.id == folderId }
        folder?.let {
            repository.saveFolder(it.copy(name = newName))
            loadFolders()
        }
    }

    fun deleteFolder(folderId: String) {
        repository.deleteFolder(folderId)
        loadFolders()
    }

    fun getNotesCount(folderId: String): Int {
        return repository.getNotesCountForFolder(folderId)
    }
}
