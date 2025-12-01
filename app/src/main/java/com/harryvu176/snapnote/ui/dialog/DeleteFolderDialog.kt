package com.harryvu176.snapnote.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.harryvu176.snapnote.R

class DeleteFolderDialog(
    private val folderName: String,
    private val onDeleteFolder: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_folder))
            .setMessage(getString(R.string.delete_folder_confirm))
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { dialog, _ ->
                onDeleteFolder()
                dialog.dismiss()
            }
            .create()
    }

    companion object {
        const val TAG = "DeleteFolderDialog"
    }
}
