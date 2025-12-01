package com.harryvu176.snapnote.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.harryvu176.snapnote.R
import com.harryvu176.snapnote.data.model.Folder
import com.harryvu176.snapnote.databinding.FragmentFoldersBinding
import com.harryvu176.snapnote.ui.adapter.FolderAdapter
import com.harryvu176.snapnote.ui.dialog.CreateFolderDialog
import com.harryvu176.snapnote.ui.dialog.DeleteFolderDialog
import com.harryvu176.snapnote.ui.dialog.RenameFolderDialog
import com.harryvu176.snapnote.viewmodel.FoldersViewModel

class FoldersFragment : Fragment() {

    private var _binding: FragmentFoldersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FoldersViewModel by activityViewModels()

    private lateinit var folderAdapter: FolderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFolders()
    }

    private fun setupRecyclerView() {
        folderAdapter = FolderAdapter(
            onFolderClick = { folder -> navigateToFolderDetail(folder) },
            onMoreClick = { folder -> showFolderOptionsMenu(folder) },
            getNotesCount = { folderId -> viewModel.getNotesCount(folderId) }
        )

        binding.foldersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = folderAdapter
        }
    }

    private fun setupClickListeners() {
        binding.createFolderFab.setOnClickListener {
            showCreateFolderDialog()
        }

        binding.unsortedNotesCard.setOnClickListener {
            // Navigate to unsorted notes (folderId = null)
            navigateToUnsortedNotes()
        }
    }

    private fun observeViewModel() {
        viewModel.folders.observe(viewLifecycleOwner) { folders ->
            folderAdapter.submitList(folders)

            if (folders.isEmpty()) {
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.foldersRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateContainer.visibility = View.GONE
                binding.foldersRecyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.unsortedNotesCount.observe(viewLifecycleOwner) { count ->
            binding.unsortedNotesCount.text = when (count) {
                0 -> getString(R.string.zero_notes)
                1 -> getString(R.string.one_note)
                else -> getString(R.string.notes_count, count)
            }
        }
    }

    private fun showCreateFolderDialog() {
        CreateFolderDialog { folderName ->
            viewModel.createFolder(folderName)
        }.show(parentFragmentManager, CreateFolderDialog.TAG)
    }

    private fun showFolderOptionsMenu(folder: Folder) {
        val anchorView = binding.foldersRecyclerView.findViewHolderForAdapterPosition(
            folderAdapter.currentList.indexOf(folder)
        )?.itemView?.findViewById<View>(R.id.moreButton) ?: return

        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.menu_folder_options, menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_rename -> {
                        showRenameFolderDialog(folder)
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteFolderDialog(folder)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showRenameFolderDialog(folder: Folder) {
        RenameFolderDialog(folder.name) { newName ->
            viewModel.renameFolder(folder.id, newName)
        }.show(parentFragmentManager, RenameFolderDialog.TAG)
    }

    private fun showDeleteFolderDialog(folder: Folder) {
        DeleteFolderDialog(folder.name) {
            viewModel.deleteFolder(folder.id)
        }.show(parentFragmentManager, DeleteFolderDialog.TAG)
    }

    private fun navigateToFolderDetail(folder: Folder) {
        val fragment = FolderDetailFragment.newInstance(folder.id, folder.name)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToUnsortedNotes() {
        val fragment = FolderDetailFragment.newInstanceForUnsorted(
            getString(R.string.unsorted_notes)
        )
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
