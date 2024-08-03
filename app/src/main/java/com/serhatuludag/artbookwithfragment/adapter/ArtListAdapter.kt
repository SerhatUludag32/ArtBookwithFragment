package com.serhatuludag.artbookwithfragment.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.serhatuludag.artbookwithfragment.databinding.FragmentListBinding
import com.serhatuludag.artbookwithfragment.databinding.RecyclerRowBinding
import com.serhatuludag.artbookwithfragment.model.Art
import com.serhatuludag.artbookwithfragment.view.ListFragment
import com.serhatuludag.artbookwithfragment.view.ListFragmentDirections

class ArtListAdapter(var artList : List<Art>) : RecyclerView.Adapter<ArtListAdapter.ArtHolder>() {



    class ArtHolder(val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val recyclerRowBinding : RecyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtHolder(recyclerRowBinding)

    }

    override fun getItemCount(): Int {
        return artList.size
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.recyclerRowBinding.recyclerViewTextView.text = artList.get(position).artName
        holder.itemView.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToSaveFragment(artList.get(position).id, "old")
            Navigation.findNavController(it).navigate(action)


        }

    }
}