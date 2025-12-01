package com.harryvu176.snapnote.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.harryvu176.snapnote.R
import com.harryvu176.snapnote.databinding.ActivityScanBinding
import com.harryvu176.snapnote.viewmodel.ScanViewModel
import java.io.File

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private val viewModel: ScanViewModel by viewModels()

    private var currentPhotoUri: Uri? = null
    private var folderId: String? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            showImagePreview()
            viewModel.processImageFromUri(currentPhotoUri!!)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        folderId = intent.getStringExtra(EXTRA_FOLDER_ID)

        setupClickListeners()
        observeViewModel()

        // Auto-launch camera on start
        if (savedInstanceState == null) {
            checkCameraPermissionAndLaunch()
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.takePhotoButton.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        binding.usePhotoButton.setOnClickListener {
            val note = viewModel.saveNote(folderId)
            // Open the note detail
            val intent = Intent(this, NoteDetailActivity::class.java).apply {
                putExtra(NoteDetailActivity.EXTRA_NOTE_ID, note.id)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.scanState.observe(this) { state ->
            when (state) {
                is ScanViewModel.ScanState.Initial -> {
                    binding.loadingIndicator.isVisible = false
                    binding.processingText.isVisible = false
                    binding.recognizedTextCard.isVisible = false
                    binding.usePhotoButton.isEnabled = false
                    binding.placeholderContainer.isVisible = true
                }
                is ScanViewModel.ScanState.Processing -> {
                    binding.loadingIndicator.isVisible = true
                    binding.processingText.isVisible = true
                    binding.recognizedTextCard.isVisible = false
                    binding.usePhotoButton.isEnabled = false
                    binding.placeholderContainer.isVisible = false
                }
                is ScanViewModel.ScanState.Success -> {
                    binding.loadingIndicator.isVisible = false
                    binding.processingText.isVisible = false
                    binding.recognizedTextCard.isVisible = true
                    binding.recognizedText.text = state.text
                    binding.usePhotoButton.isEnabled = true
                    binding.takePhotoButton.text = getString(R.string.retake)
                }
                is ScanViewModel.ScanState.NoTextFound -> {
                    binding.loadingIndicator.isVisible = false
                    binding.processingText.isVisible = false
                    binding.recognizedTextCard.isVisible = true
                    binding.recognizedText.text = getString(R.string.no_text_found)
                    binding.usePhotoButton.isEnabled = false
                    binding.takePhotoButton.text = getString(R.string.retake)
                    Toast.makeText(this, R.string.no_text_found, Toast.LENGTH_SHORT).show()
                }
                is ScanViewModel.ScanState.Error -> {
                    binding.loadingIndicator.isVisible = false
                    binding.processingText.isVisible = false
                    binding.usePhotoButton.isEnabled = false
                    binding.takePhotoButton.text = getString(R.string.retake)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        val photoFile = createImageFile()
        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(currentPhotoUri!!)
    }

    private fun createImageFile(): File {
        val timestamp = System.currentTimeMillis()
        val imageFileName = "SCAN_${timestamp}"
        val storageDir = getExternalFilesDir("images")
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun showImagePreview() {
        currentPhotoUri?.let { uri ->
            binding.placeholderContainer.isVisible = false
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imagePreview.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val EXTRA_FOLDER_ID = "folder_id"
    }
}
