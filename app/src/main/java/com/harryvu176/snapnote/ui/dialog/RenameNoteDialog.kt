package com.harryvu176.snapnote.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.harryvu176.snapnote.databinding.DialogRenameNoteBinding

class RenameNoteDialog(
    private val currentTitle: String,
    private val onRenameNote: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogRenameNoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRenameNoteBinding.inflate(layoutInflater)

        binding.noteTitleInput.setText(currentTitle)
        binding.noteTitleInput.selectAll()

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.renameButton.setOnClickListener {
            val title = binding.noteTitleInput.text?.toString()?.trim()
            if (!title.isNullOrEmpty()) {
                onRenameNote(title)
                dismiss()
            } else {
                binding.noteTitleLayout.error = "Please enter a title"
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RenameNoteDialog"
    }
}
