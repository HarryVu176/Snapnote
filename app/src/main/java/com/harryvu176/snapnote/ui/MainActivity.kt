package com.harryvu176.snapnote.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.harryvu176.snapnote.R
import com.harryvu176.snapnote.databinding.ActivityMainBinding

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

        if (savedInstanceState == null) {
            loadFragment(NotesFragment(), clearBackStack = true)
            currentTab = Tab.NOTES
            updateSelectedButton(Tab.NOTES)
        } else {
            // Restore current tab
            currentTab = Tab.values()[savedInstanceState.getInt(KEY_CURRENT_TAB, 0)]
            updateSelectedButton(currentTab)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_TAB, currentTab.ordinal)
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
            // When back stack is empty, we're at the root fragment
            // Update selected button based on current fragment
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
                    // If we're on a different tab, go to Notes first
                    if (currentTab != Tab.NOTES) {
                        loadFragment(NotesFragment(), clearBackStack = true)
                        currentTab = Tab.NOTES
                        updateSelectedButton(Tab.NOTES)
                    } else {
                        // Exit the app
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    private fun loadFragment(fragment: Fragment, clearBackStack: Boolean = false) {
        if (clearBackStack) {
            // Clear back stack when switching tabs
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
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
