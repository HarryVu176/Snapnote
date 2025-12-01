package com.harryvu176.snapnote.data.model

data class Folder(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
