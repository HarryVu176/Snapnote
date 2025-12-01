package com.harryvu176.snapnote.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.harryvu176.snapnote.databinding.DialogRenameFolderBinding

class RenameFolderDialog(
    private val currentName: String,
    private val onRenameFolder: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogRenameFolderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRenameFolderBinding.inflate(layoutInflater)

        binding.folderNameInput.setText(currentName)
        binding.folderNameInput.selectAll()

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.renameButton.setOnClickListener {
            val name = binding.folderNameInput.text?.toString()?.trim()
            if (!name.isNullOrEmpty()) {
                onRenameFolder(name)
                dismiss()
            } else {
                binding.folderNameLayout.error = "Please enter a folder name"
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
        const val TAG = "RenameFolderDialog"
    }
}
