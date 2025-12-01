package com.harryvu176.snapnote.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.harryvu176.snapnote.R
import com.harryvu176.snapnote.data.model.Note
import com.harryvu176.snapnote.databinding.FragmentNotesBinding
import com.harryvu176.snapnote.ui.adapter.NoteAdapter
import com.harryvu176.snapnote.ui.dialog.DeleteNoteDialog
import com.harryvu176.snapnote.ui.dialog.RenameNoteDialog
import com.harryvu176.snapnote.viewmodel.NotesViewModel

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by activityViewModels()
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadNotes()
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

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchNotes(s?.toString() ?: "")
            }
        })
    }

    private fun setupClickListeners() {
        binding.scanNewFab.setOnClickListener {
            startActivity(Intent(requireContext(), ScanActivity::class.java))
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
