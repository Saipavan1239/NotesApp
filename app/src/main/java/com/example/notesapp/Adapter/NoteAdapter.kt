package com.example.notesapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.Fragments.HomeFragmentDirections
import com.example.notesapp.Model.Note
import com.example.notesapp.databinding.NoteLayoutBinding

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(val itemBinding: NoteLayoutBinding): RecyclerView.ViewHolder(itemBinding.root)

    private val differCallbacks = object : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallbacks)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(NoteLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentItem = differ.currentList[position]

        holder.itemBinding.noteTitle.text = currentItem.noteTitle
        holder.itemBinding.noteDesc.text = currentItem.noteDescription

        holder.itemView.setOnClickListener{
            // TODO: Navigate to note detail screen
            val direction = HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(currentItem)
            it.findNavController().navigate(direction)
        }

    }


}
