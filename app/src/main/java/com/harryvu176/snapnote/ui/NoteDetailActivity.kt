package com.harryvu176.snapnote.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.harryvu176.snapnote.R
import com.harryvu176.snapnote.databinding.ActivityNoteDetailBinding
import com.harryvu176.snapnote.viewmodel.NoteDetailViewModel

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private val viewModel: NoteDetailViewModel by viewModels()

    private var isImageVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        if (noteId == null) {
            finish()
            return
        }

        setupClickListeners()
        observeViewModel()

        viewModel.loadNote(noteId)
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.editButton.setOnClickListener {
            viewModel.setEditMode(true)
        }

        binding.saveButton.setOnClickListener {
            val title = binding.noteTitleText.text?.toString() ?: ""
            val content = binding.noteContentEditText.text?.toString() ?: ""
            viewModel.saveNote(title, content)
        }

        binding.toggleImageButton.setOnClickListener {
            toggleImageVisibility()
        }
    }

    private fun observeViewModel() {
        viewModel.note.observe(this) { note ->
            note?.let {
                binding.noteTitleText.text = it.title
                binding.noteContentEditText.setText(it.content)

                // Load image if available
                if (!it.imageUri.isNullOrEmpty()) {
                    try {
                        binding.originalImageView.setImageURI(it.imageUri.toUri())
                        binding.toggleImageButton.isVisible = true
                    } catch (e: Exception) {
                        binding.toggleImageButton.isVisible = false
                    }
                } else {
                    binding.toggleImageButton.isVisible = false
                }
            }
        }

        viewModel.isEditMode.observe(this) { isEditMode ->
            binding.noteContentEditText.isEnabled = isEditMode
            binding.editButton.isVisible = !isEditMode
            binding.saveButton.isVisible = isEditMode

            if (isEditMode) {
                binding.noteContentEditText.requestFocus()
            }
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleImageVisibility() {
        isImageVisible = !isImageVisible
        binding.originalImageCard.isVisible = isImageVisible
        binding.toggleImageButton.text = getString(
            if (isImageVisible) R.string.hide_original_image else R.string.show_original_image
        )
    }

    companion object {
        const val EXTRA_NOTE_ID = "note_id"
    }
}
