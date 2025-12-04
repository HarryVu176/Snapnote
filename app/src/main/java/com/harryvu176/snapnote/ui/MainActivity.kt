package com.harryvu176.snapnote.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.harryvu176.snapnote.BuildConfig
import com.harryvu176.snapnote.R
import com.harryvu176.snapnote.data.model.Note
import com.harryvu176.snapnote.data.repository.NoteRepository
import com.harryvu176.snapnote.databinding.ActivityMainBinding
import org.json.JSONArray
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentTab: Tab = Tab.NOTES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()
        setupBackStackListener()
        setupBackPressedCallback()

        if (BuildConfig.DEBUG) {
            preloadTestData()
        }

        prefetchTranslationModels()

        if (savedInstanceState == null) {
            loadFragment(NotesFragment(), clearBackStack = true)
            currentTab = Tab.NOTES
            updateSelectedButton(Tab.NOTES)
        } else {
            currentTab = Tab.entries.toTypedArray()[savedInstanceState.getInt(KEY_CURRENT_TAB, 0)]
            updateSelectedButton(currentTab)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_TAB, currentTab.ordinal)
    }

    private fun prefetchTranslationModels() {
        val languages = listOf(
            TranslateLanguage.FRENCH
        )

        val modelManager = RemoteModelManager.getInstance()
        
        val conditions = DownloadConditions.Builder()
            .build()

        languages.forEach { language ->
            val model = TranslateRemoteModel.Builder(language).build()
            modelManager.download(model, conditions)
                .addOnSuccessListener {
                   Toast.makeText(this, "Pre-fetched model: $language", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to pre-fetch model: $language", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun preloadTestData() {
        val repository = NoteRepository(this)
        if (repository.getAllNotes().isNotEmpty()) return

        try {
            val jsonString = assets.open("sample_notes.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val note = Note(
                    id = UUID.randomUUID().toString(),
                    title = obj.getString("title"),
                    content = obj.getString("content"),
                    translation = null,
                    folderId = null
                )
                repository.saveNote(note)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupNavigation() {
        binding.navNotesButton.setOnClickListener {
            if (currentTab != Tab.NOTES) {
                loadFragment(NotesFragment(), clearBackStack = true)
                currentTab = Tab.NOTES
                updateSelectedButton(Tab.NOTES)
            }
        }

        binding.navFoldersButton.setOnClickListener {
            if (currentTab != Tab.FOLDERS) {
                loadFragment(FoldersFragment(), clearBackStack = true)
                currentTab = Tab.FOLDERS
                updateSelectedButton(Tab.FOLDERS)
            }
        }

        binding.navAboutButton.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
        }
    }

    private fun setupBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            when (currentFragment) {
                is NotesFragment -> {
                    currentTab = Tab.NOTES
                    updateSelectedButton(Tab.NOTES)
                }
                is FoldersFragment -> {
                    currentTab = Tab.FOLDERS
                    updateSelectedButton(Tab.FOLDERS)
                }
            }
        }
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    if (currentTab != Tab.NOTES) {
                        loadFragment(NotesFragment(), clearBackStack = true)
                        currentTab = Tab.NOTES
                        updateSelectedButton(Tab.NOTES)
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    private fun loadFragment(fragment: Fragment, clearBackStack: Boolean = false) {
        if (clearBackStack) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStackImmediate(
                    null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun updateSelectedButton(selectedTab: Tab) {
        val selectedColor = getColor(R.color.brand_primary)
        val unselectedColor = getColor(R.color.brand_secondary)

        binding.navNotesButton.setTextColor(if (selectedTab == Tab.NOTES) selectedColor else unselectedColor)
        binding.navNotesButton.iconTint = android.content.res.ColorStateList.valueOf(
            if (selectedTab == Tab.NOTES) selectedColor else unselectedColor
        )

        binding.navFoldersButton.setTextColor(if (selectedTab == Tab.FOLDERS) selectedColor else unselectedColor)
        binding.navFoldersButton.iconTint = android.content.res.ColorStateList.valueOf(
            if (selectedTab == Tab.FOLDERS) selectedColor else unselectedColor
        )

        binding.navAboutButton.setTextColor(unselectedColor)
        binding.navAboutButton.iconTint = android.content.res.ColorStateList.valueOf(unselectedColor)
    }

    private enum class Tab {
        NOTES, FOLDERS
    }

    companion object {
        private const val KEY_CURRENT_TAB = "current_tab"
    }
}
