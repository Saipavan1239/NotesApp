    package com.example.notesapp.Fragments

    import android.content.Intent
    import android.os.Bundle
    import androidx.fragment.app.Fragment
    import android.view.LayoutInflater
    import android.view.Menu
    import android.view.MenuInflater
    import android.view.MenuItem
    import android.view.View
    import android.view.ViewGroup
    import androidx.appcompat.widget.MenuPopupWindow
    import androidx.appcompat.widget.SearchView
    import androidx.core.view.MenuHost
    import androidx.core.view.MenuProvider
    import androidx.lifecycle.Lifecycle
    import androidx.navigation.findNavController
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.StaggeredGridLayoutManager
    import com.example.notesapp.Adapter.NoteAdapter
    import com.example.notesapp.MainActivity
    import com.example.notesapp.Model.Note
    import com.example.notesapp.R
    import com.example.notesapp.ViewModel.NoteViewModel
    import com.example.notesapp.databinding.FragmentHomeBinding
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore

    class HomeFragment : Fragment(R.layout.fragment_home) , SearchView.OnQueryTextListener , MenuProvider{

        private var homeBinding : FragmentHomeBinding? =null
        private val binding get() = homeBinding!!

        private lateinit var notesViewModel : NoteViewModel
        private lateinit var noteAdapter : NoteAdapter

        private lateinit var auth: FirebaseAuth
        private lateinit var firestore: FirebaseFirestore
        private lateinit var uid: String

        private fun notesRef() =
            firestore.collection("users")
                .document(uid)
                .collection("notes")


        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            // Inflate the layout for this fragment
            homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val menuHost:MenuHost = requireActivity()
            menuHost.addMenuProvider(this, viewLifecycleOwner,Lifecycle.State.RESUMED)

            notesViewModel = (activity as MainActivity).noteViewModel
            setUpHomeRecyclerView()

            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()

            val user = auth.currentUser
            if (user == null) {
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
                return
            }

            uid = user.uid


            binding.addNoteFab.setOnClickListener{
                it.findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
            }
        }

        private fun syncRoomToFirestore(notes: List<Note>) {
            for (note in notes) {
                val firestoreNote = hashMapOf(
                    "id" to note.id,
                    "title" to note.noteTitle,
                    "description" to note.noteDescription,
                    "updatedAt" to System.currentTimeMillis()
                )

                notesRef()
                    .document(note.id.toString())
                    .set(firestoreNote)
            }
        }




        private fun updateUI(note: List<Note>){
            if(note != null){
                if(note.isNotEmpty()){
                    binding.emptyNotesImage.visibility  = View.GONE
                    binding.homeRecyclerView.visibility = View.VISIBLE
                }
                else{
                    binding.emptyNotesImage.visibility  = View.VISIBLE
                    binding.homeRecyclerView.visibility = View.GONE
                }
            }
        }

        private fun setUpHomeRecyclerView() {
            noteAdapter = NoteAdapter()
            binding.homeRecyclerView.apply {
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                setHasFixedSize(true)
                adapter = noteAdapter
            }

            activity?.let {
                notesViewModel.getAllNotes().observe(viewLifecycleOwner) { notes ->
                    noteAdapter.differ.submitList(notes)
                    updateUI(notes)

                    if (notes.isNotEmpty()) {
                        syncRoomToFirestore(notes)
                    } else {

                        syncFirestoreToRoom()
                    }
                }
            }
        }

        private fun syncFirestoreToRoom() {
            notesRef().get().addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    val note = Note(
                        id = doc.getLong("id")?.toInt() ?: continue,
                        noteTitle = doc.getString("title") ?: "",
                        noteDescription = doc.getString("description") ?: ""
                    )
                    notesViewModel.addNote(note)
                }
            }
        }


        private fun searchNote(query: String?) {
            val safeQuery = query?.trim() ?: ""

            notesViewModel.searchNotes(safeQuery).observe(viewLifecycleOwner) { list ->
                noteAdapter.differ.submitList(list)
            }
        }




        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            searchNote(newText)
            return true
        }


        override fun onDestroyView() {
            super.onDestroyView()
            homeBinding = null
        }

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menu.clear()
            menuInflater.inflate(R.menu.home_menu, menu)

            val menuSearch = menu.findItem(R.id.searchMenu).actionView as SearchView
            menuSearch.isSubmitButtonEnabled = false
            menuSearch.setOnQueryTextListener(this)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return false
        }

    }