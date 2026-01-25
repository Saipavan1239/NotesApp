package com.example.notesapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notesapp.MainActivity
import com.example.notesapp.Model.Note
import com.example.notesapp.R
import com.example.notesapp.ViewModel.NoteViewModel
import com.example.notesapp.databinding.FragmentEditNoteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditNoteFragment : Fragment(), MenuProvider {

    private var editNoteBinding : FragmentEditNoteBinding?= null
    private val binding get() = editNoteBinding!!

    private lateinit var notesViewModel : NoteViewModel
    private lateinit var currentNote : Note

    private val args: EditNoteFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        editNoteBinding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        notesViewModel = (activity as MainActivity).noteViewModel
        currentNote = args.note ?: run {
            Toast.makeText(context, "Note not found", Toast.LENGTH_SHORT).show()
            view.findNavController().popBackStack()
            return
        }


        binding.editNoteTitle.setText(currentNote.noteTitle)
        binding.editNoteDesc.setText(currentNote.noteDescription)

        binding.deleteNoteFab.setOnClickListener {
            deleteNote()
        }
    }

    private fun deleteNote() {
        activity?.let {
            AlertDialog.Builder(it).apply {
                setTitle("Delete Note")
                setMessage("Are you sure you want to delete this note?")
                setPositiveButton("Delete") { _, _ ->
                    notesViewModel.deleteNode(currentNote)
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .collection("notes")
                        .document(currentNote.id.toString())
                        .delete()

                    Toast.makeText(context,"Note Deleted",Toast.LENGTH_SHORT).show()
                    view?.findNavController()?.popBackStack(R.id.homeFragment, false)
                }
                setNegativeButton("Cancel",null)
            }.create().show()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.menu_edit_note, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.saveMenu -> {
                val noteTitle = binding.editNoteTitle.text.toString().trim()
                val noteDesc = binding.editNoteDesc.text.toString().trim()

                if (noteTitle.isNotEmpty()) {
                    val note = Note(
                        id = currentNote.id,
                        noteTitle = noteTitle,
                        noteDescription = noteDesc
                    )
                    notesViewModel.updateNode(note)
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .collection("notes")
                        .document(note.id.toString())
                        .update(
                            mapOf(
                                "title" to note.noteTitle,
                                "description" to note.noteDescription,
                                "updatedAt" to System.currentTimeMillis()
                            )
                        )

                    view?.findNavController()
                        ?.popBackStack(R.id.homeFragment, false)
                } else {
                    Toast.makeText(
                        context,
                        "Please enter a note title",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            else -> false
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        editNoteBinding = null
    }


}