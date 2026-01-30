package com.example.notesapp.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.notesapp.Database.NotesDatabase
import com.example.notesapp.Database.UserProfileEntity
import com.example.notesapp.R
import com.example.notesapp.Repository.UserProfileRepository
import com.example.notesapp.ViewModel.ProfileViewModel
import com.example.notesapp.ViewModel.ProfileViewModelFactory
import com.example.notesapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var uid: String

    /* ---------- IMAGE PICKER ---------- */
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    saveImageLocally(uri)
                }
            }
        }


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



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uid = FirebaseAuth.getInstance().currentUser!!.uid

        val repository = UserProfileRepository(
            NotesDatabase(requireContext()),
            FirebaseFirestore.getInstance()
        )

        viewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(repository)
        )[ProfileViewModel::class.java]

        // LOAD DATA
        viewModel.loadProfile(uid)

        // OBSERVERS
        viewModel.name.observe(viewLifecycleOwner) {
            binding.nameOnImage.text = it
        }

        viewModel.about.observe(viewLifecycleOwner) {
            binding.aboutTxt.text = it
        }

        viewModel.profileImage.observe(viewLifecycleOwner) { profile ->
            if (profile?.imagePath != null) {
                viewModel.profileImage.observe(viewLifecycleOwner) { profile ->
                    if (profile?.imagePath != null) {
                        val bitmap = loadScaledBitmap(
                            profile.imagePath,
                            1080,   // screen width
                            1200    // header height
                        )
                        binding.profileImage.setImageBitmap(bitmap)
                    } else {
                        binding.profileImage.setImageResource(R.drawable.ic_person)
                    }
                }


            } else {
                binding.profileImage.setImageResource(R.drawable.ic_person)
            }
        }

        // BACK
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        // IMAGE MENU
        binding.menuBtn.setOnClickListener {
            showImageMenu()
        }

        // EDIT NAME
        binding.editNameBtn.setOnClickListener {
            showEditDialog(
                title = "Edit Name",
                initial = binding.nameOnImage.text.toString()
            ) {
                viewModel.updateName(uid, it)
            }
        }

        // EDIT ABOUT
        binding.editAboutBtn.setOnClickListener {
            showEditDialog(
                title = "Edit About",
                initial = binding.aboutTxt.text.toString()
            ) {
                viewModel.updateAbout(uid, it)
            }
        }

        // LOGOUT
        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    /* ---------- IMAGE HELPERS ---------- */

    private fun showImageMenu() {
        AlertDialog.Builder(requireContext())
            .setItems(arrayOf("Upload Photo", "Remove Photo")) { _, which ->
                if (which == 0) openGallery()
                else viewModel.removeProfileImage(uid)
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePicker.launch(intent)
    }

    private fun saveImageLocally(uri: Uri) {
        val input = requireContext().contentResolver.openInputStream(uri) ?: return
        val file = File(requireContext().filesDir, "profile_$uid.jpg")

        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }

        viewModel.saveProfileImage(
            UserProfileEntity(uid = uid, imagePath = file.absolutePath)
        )
    }

    /* ---------- DIALOG ---------- */

    private fun showEditDialog(
        title: String,
        initial: String,
        onSave: (String) -> Unit
    ) {
        val editText = android.widget.EditText(requireContext())
        editText.setText(initial)

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                onSave(editText.text.toString().trim())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}