package com.harryvu176.snapnote.ui.adapter

import android.net.Uri
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.harryvu176.snapnote.data.model.Note
import com.harryvu176.snapnote.databinding.ItemNoteBinding
import java.util.Date

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onMoreClick: (Note, View) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.noteTitle.text = note.title
            binding.notePreview.text = note.content.take(100)

            // Format date
            val dateFormat = DateFormat.getMediumDateFormat(binding.root.context)
            binding.noteDate.text = dateFormat.format(Date(note.updatedAt))

            // Load thumbnail
            if (!note.imageUri.isNullOrEmpty()) {
                try {
                    binding.noteThumbnail.setImageURI(Uri.parse(note.imageUri))
                    binding.noteThumbnail.visibility = View.VISIBLE
                    binding.thumbnailPlaceholder.visibility = View.GONE
                } catch (e: Exception) {
                    showPlaceholder()
                }
            } else {
                showPlaceholder()
            }

            binding.noteCard.setOnClickListener {
                onNoteClick(note)
            }

            binding.moreButton.setOnClickListener { view ->
                onMoreClick(note, view)
            }
        }

        private fun showPlaceholder() {
            binding.noteThumbnail.visibility = View.GONE
            binding.thumbnailPlaceholder.visibility = View.VISIBLE
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
