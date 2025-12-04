package com.harryvu176.snapnote.data.model

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val imageUri: String? = null,
    val folderId: String? = null,
    val translation: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
