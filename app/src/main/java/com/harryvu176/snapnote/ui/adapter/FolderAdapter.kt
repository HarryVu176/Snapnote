package com.harryvu176.snapnote.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.harryvu176.snapnote.R
import com.harryvu176.snapnote.data.model.Folder
import com.harryvu176.snapnote.databinding.ItemFolderBinding

class FolderAdapter(
    private val onFolderClick: (Folder) -> Unit,
    private val onMoreClick: (Folder) -> Unit,
    private val getNotesCount: (String) -> Int
) : ListAdapter<Folder, FolderAdapter.FolderViewHolder>(FolderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FolderViewHolder(
        private val binding: ItemFolderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: Folder) {
            binding.folderName.text = folder.name

            val count = getNotesCount(folder.id)
            binding.notesCount.text = when (count) {
                0 -> binding.root.context.getString(R.string.zero_notes)
                1 -> binding.root.context.getString(R.string.one_note)
                else -> binding.root.context.getString(R.string.notes_count, count)
            }

            binding.folderCard.setOnClickListener {
                onFolderClick(folder)
            }

            binding.moreButton.setOnClickListener {
                onMoreClick(folder)
            }
        }
    }

    class FolderDiffCallback : DiffUtil.ItemCallback<Folder>() {
        override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem == newItem
        }
    }
}
