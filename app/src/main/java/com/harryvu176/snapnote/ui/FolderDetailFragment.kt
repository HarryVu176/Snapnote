package com.harryvu176.snapnote.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.harryvu176.snapnote.R
import com.harryvu176.snapnote.data.model.Note
import com.harryvu176.snapnote.databinding.FragmentFolderDetailBinding
import com.harryvu176.snapnote.ui.adapter.NoteAdapter
import com.harryvu176.snapnote.ui.dialog.DeleteNoteDialog
import com.harryvu176.snapnote.ui.dialog.RenameFolderDialog
import com.harryvu176.snapnote.ui.dialog.RenameNoteDialog
import com.harryvu176.snapnote.viewmodel.FolderDetailViewModel

class FolderDetailFragment : Fragment() {

    private var _binding: FragmentFolderDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FolderDetailViewModel by activityViewModels()
    private lateinit var noteAdapter: NoteAdapter

    private var folderId: String? = null
    private var folderName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            folderId = it.getString(ARG_FOLDER_ID)
            folderName = it.getString(ARG_FOLDER_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFolderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.folderTitle.text = folderName

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        folderId?.let { viewModel.loadNotesForFolder(it) }
    }

    override fun onResume() {
        super.onResume()
        folderId?.let { viewModel.loadNotesForFolder(it) }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            onNoteClick = { note -> navigateToNoteDetail(note) },
            onMoreClick = { note, view -> showNoteOptionsMenu(note, view) }
        )

        binding.notesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = noteAdapter
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.editButton.setOnClickListener {
            showRenameDialog()
        }

        binding.scanNoteFab.setOnClickListener {
            val intent = Intent(requireContext(), ScanActivity::class.java).apply {
                putExtra(ScanActivity.EXTRA_FOLDER_ID, folderId)
            }
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes)

            if (notes.isEmpty()) {
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.notesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateContainer.visibility = View.GONE
                binding.notesRecyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.folderName.observe(viewLifecycleOwner) { name ->
            binding.folderTitle.text = name
            folderName = name
        }
    }

    private fun navigateToNoteDetail(note: Note) {
        val intent = Intent(requireContext(), NoteDetailActivity::class.java).apply {
            putExtra(NoteDetailActivity.EXTRA_NOTE_ID, note.id)
        }
        startActivity(intent)
    }

    private fun showNoteOptionsMenu(note: Note, anchorView: View) {
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.menu_note_options, menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_rename -> {
                        showRenameNoteDialog(note)
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteNoteDialog(note)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showRenameNoteDialog(note: Note) {
        RenameNoteDialog(note.title) { newTitle ->
            viewModel.renameNote(note.id, newTitle)
        }.show(parentFragmentManager, RenameNoteDialog.TAG)
    }

    private fun showDeleteNoteDialog(note: Note) {
        DeleteNoteDialog {
            viewModel.deleteNote(note.id)
        }.show(parentFragmentManager, DeleteNoteDialog.TAG)
    }

    private fun showRenameDialog() {
        RenameFolderDialog(folderName ?: "") { newName ->
            folderId?.let { id ->
                viewModel.renameFolder(id, newName)
            }
        }.show(parentFragmentManager, RenameFolderDialog.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_FOLDER_ID = "folder_id"
        private const val ARG_FOLDER_NAME = "folder_name"

        fun newInstance(folderId: String, folderName: String): FolderDetailFragment {
            return FolderDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FOLDER_ID, folderId)
                    putString(ARG_FOLDER_NAME, folderName)
                }
            }
        }
    }
}
