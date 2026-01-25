package com.example.notesapp

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.example.notesapp.Database.NotesDatabase
import com.example.notesapp.Repository.NoteRepository
import com.example.notesapp.ViewModel.NoteViewModel
import com.example.notesapp.ViewModel.NoteViewModelFactory
import com.example.notesapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var noteViewModel: NoteViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Setup ViewModel
        setUpViewModel()

        // Apply saved theme
        applyTheme()

        // Setup Theme Switch Button
        binding.themeSwitchButton.setOnClickListener {
            switchTheme()
        }
    }

    private fun setUpViewModel() {
        val noteRepository = NoteRepository(NotesDatabase(this))
        val viewModelProviderFactory = NoteViewModelFactory(application, noteRepository)
        noteViewModel = ViewModelProvider(this, viewModelProviderFactory).get(NoteViewModel::class.java)
    }

    private fun switchTheme() {
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        val newNightMode = if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(newNightMode)
        saveThemePreference(newNightMode)

        // Update icon based on the new theme
        updateThemeSwitchIcon(newNightMode)

        // Recreate the activity to apply theme changes
        recreate()
    }

    private fun applyTheme() {
        val savedNightMode = sharedPreferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(savedNightMode)

        // Ensure binding is initialized before accessing it
        updateThemeSwitchIcon(savedNightMode)
    }

    private fun updateThemeSwitchIcon(nightMode: Int) {
        val iconRes = if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            R.drawable.baseline_sunny_24 // Show sun icon in dark mode
        } else {
            R.drawable.baseline_mode_night_24 // Show moon icon in light mode
        }
        binding.themeSwitchButton.setImageResource(iconRes)
    }

    private fun saveThemePreference(nightMode: Int) {
        with(sharedPreferences.edit()) {
            putInt("night_mode", nightMode)
            apply()
        }
    }
}
