package com.harryvu176.snapnote.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.harryvu176.snapnote.databinding.DialogCreateFolderBinding

class CreateFolderDialog(
    private val onCreateFolder: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogCreateFolderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCreateFolderBinding.inflate(LayoutInflater.from(context))

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.createButton.setOnClickListener {
            val name = binding.folderNameInput.text?.toString()?.trim()
            if (!name.isNullOrEmpty()) {
                onCreateFolder(name)
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
        const val TAG = "CreateFolderDialog"
    }
}
