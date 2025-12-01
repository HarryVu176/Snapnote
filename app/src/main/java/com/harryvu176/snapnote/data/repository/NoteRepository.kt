package com.harryvu176.snapnote.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.harryvu176.snapnote.data.model.Folder
import com.harryvu176.snapnote.data.model.Note
import org.json.JSONArray
import org.json.JSONObject

class NoteRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAllNotes(): List<Note> {
        val json = prefs.getString(KEY_NOTES, "[]") ?: "[]"
        return parseNotes(json)
    }

    fun getNotesByFolder(folderId: String?): List<Note> {
        return getAllNotes().filter { it.folderId == folderId }
    }

    fun getUnsortedNotes(): List<Note> {
        return getAllNotes().filter { it.folderId == null }
    }

    fun saveNote(note: Note) {
        val notes = getAllNotes().toMutableList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            notes[index] = note
        } else {
            notes.add(note)
        }
        saveNotes(notes)
    }

    fun deleteNote(noteId: String) {
        val notes = getAllNotes().filter { it.id != noteId }
        saveNotes(notes)
    }

    fun getAllFolders(): List<Folder> {
        val json = prefs.getString(KEY_FOLDERS, "[]") ?: "[]"
        return parseFolders(json)
    }

    fun saveFolder(folder: Folder) {
        val folders = getAllFolders().toMutableList()
        val index = folders.indexOfFirst { it.id == folder.id }
        if (index >= 0) {
            folders[index] = folder
        } else {
            folders.add(folder)
        }
        saveFolders(folders)
    }

    fun deleteFolder(folderId: String) {
        val folders = getAllFolders().filter { it.id != folderId }
        saveFolders(folders)

        // Move notes from deleted folder to unsorted
        val notes = getAllNotes().map { note ->
            if (note.folderId == folderId) note.copy(folderId = null) else note
        }
        saveNotes(notes)
    }

    fun getNotesCountForFolder(folderId: String): Int {
        return getAllNotes().count { it.folderId == folderId }
    }

    private fun saveNotes(notes: List<Note>) {
        val jsonArray = JSONArray()
        notes.forEach { note ->
            jsonArray.put(JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("content", note.content)
                put("imageUri", note.imageUri)
                put("folderId", note.folderId)
                put("createdAt", note.createdAt)
                put("updatedAt", note.updatedAt)
            })
        }
        prefs.edit().putString(KEY_NOTES, jsonArray.toString()).apply()
    }

    private fun saveFolders(folders: List<Folder>) {
        val jsonArray = JSONArray()
        folders.forEach { folder ->
            jsonArray.put(JSONObject().apply {
                put("id", folder.id)
                put("name", folder.name)
                put("createdAt", folder.createdAt)
            })
        }
        prefs.edit().putString(KEY_FOLDERS, jsonArray.toString()).apply()
    }

    private fun parseNotes(json: String): List<Note> {
        val notes = mutableListOf<Note>()
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            notes.add(Note(
                id = obj.getString("id"),
                title = obj.getString("title"),
                content = obj.getString("content"),
                imageUri = obj.optString("imageUri", null),
                folderId = obj.optString("folderId", null),
                createdAt = obj.getLong("createdAt"),
                updatedAt = obj.getLong("updatedAt")
            ))
        }
        return notes
    }

    private fun parseFolders(json: String): List<Folder> {
        val folders = mutableListOf<Folder>()
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            folders.add(Folder(
                id = obj.getString("id"),
                name = obj.getString("name"),
                createdAt = obj.getLong("createdAt")
            ))
        }
        return folders
    }

    companion object {
        private const val PREFS_NAME = "snapnote_prefs"
        private const val KEY_NOTES = "notes"
        private const val KEY_FOLDERS = "folders"
    }
}
