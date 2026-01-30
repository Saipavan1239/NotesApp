package com.example.notesapp

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.notesapp.Database.NotesDatabase
import com.example.notesapp.Repository.NoteRepository
import com.example.notesapp.Repository.UserProfileRepository
import com.example.notesapp.ViewModel.NoteViewModel
import com.example.notesapp.ViewModel.NoteViewModelFactory
import com.example.notesapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var noteViewModel: NoteViewModel
    private lateinit var sharedPreferences: SharedPreferences

    private fun loadScaledBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(path, options)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (
                halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // 🔥 ENSURE USER PROFILE EXISTS (ONCE)
        FirebaseAuth.getInstance().currentUser?.let { user ->
            UserProfileRepository(
                NotesDatabase(this),
                FirebaseFirestore.getInstance()
            ).ensureUserProfileExists(user.uid)
        }
        setUpViewModel()

        applyTheme()

        binding.themeSwitchButton.setOnClickListener {
            switchTheme()
        }

        getProfilePhoto()

        binding.profileButton.setOnClickListener {
            val navController = findNavController(R.id.fragmentContainerView)
            val currentDest = navController.currentDestination?.id

            if (currentDest == R.id.homeFragment) {
                navController.navigate(R.id.action_homeFragment_to_profileFragment)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        getProfilePhoto()
    }

    private fun getProfilePhoto() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val uid = user.uid

            lifecycleScope.launch {
                val profile =
                    NotesDatabase(this@MainActivity)
                        .getUserProfileDao()
                        .getProfile(uid)

                if (profile?.imagePath != null) {
                    val bitmap = loadScaledBitmap(profile.imagePath, 120, 120)
                    binding.profileButton.clearColorFilter()
                    binding.profileButton.setImageBitmap(bitmap)
                    binding.profileButton.scaleType = ImageView.ScaleType.CENTER_CROP
                } else {
                    // 🔥 fallback icon
                    binding.profileButton.setImageResource(R.drawable.ic_person)
                    binding.profileButton.setColorFilter(
                        getColor(R.color.profile_icon)
                    )
                    binding.profileButton.scaleType = ImageView.ScaleType.CENTER_INSIDE
                }

            }
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

        updateThemeSwitchIcon(newNightMode)

        recreate()
    }

    private fun applyTheme() {
        val savedNightMode = sharedPreferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(savedNightMode)

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
