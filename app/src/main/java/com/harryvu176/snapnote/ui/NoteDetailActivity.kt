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

        binding.translateButton.setOnClickListener {
            viewModel.translateNote()
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

                // Show/hide translation based on its existence
                if (!it.translation.isNullOrEmpty()) {
                    binding.translatedContentLayout.isVisible = true
                    binding.translationTextView.text = it.translation
                    binding.translationProgressBar.isVisible = false
                } else {
                    // Only hide if NOT currently translating
                    if (viewModel.isTranslating.value != true) {
                        binding.translatedContentLayout.isVisible = false
                    }
                }
            }
        }

        viewModel.isEditMode.observe(this) { isEditMode ->
            // Instead of disabling the view (which greys out text), we just make it non-focusable/read-only
            // This ensures high contrast text at all times.
            with(binding.noteContentEditText) {
                isFocusable = isEditMode
                isFocusableInTouchMode = isEditMode
                isCursorVisible = isEditMode
                isLongClickable = isEditMode 
                
                if (isEditMode) {
                    requestFocus()
                    setSelection(text?.length ?: 0)
                }
            }

            binding.editButton.isVisible = !isEditMode
            binding.saveButton.isVisible = isEditMode
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isTranslating.observe(this) { isTranslating ->
            if (isTranslating) {
                binding.translatedContentLayout.isVisible = true
                binding.translationTextView.text = getString(R.string.translating)
                binding.translationProgressBar.isVisible = true
                binding.translateButton.isEnabled = false
            } else {
                binding.translationProgressBar.isVisible = false
                binding.translateButton.isEnabled = true
                
                // If translation exists, show it (handled by note observer usually, but double check here)
                val currentNote = viewModel.note.value
                if (currentNote?.translation != null) {
                    binding.translatedContentLayout.isVisible = true
                    binding.translationTextView.text = currentNote.translation
                } else {
                    binding.translatedContentLayout.isVisible = false
                }
            }
        }

        viewModel.translationError.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, "${getString(R.string.translation_error)}: $it", Toast.LENGTH_LONG).show()
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
