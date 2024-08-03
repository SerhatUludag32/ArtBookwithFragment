package com.serhatuludag.artbookwithfragment.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.serhatuludag.artbookwithfragment.R
import com.serhatuludag.artbookwithfragment.adapter.ArtListAdapter
import com.serhatuludag.artbookwithfragment.database.ArtDao
import com.serhatuludag.artbookwithfragment.database.ArtDatabase
import com.serhatuludag.artbookwithfragment.databinding.FragmentListBinding
import com.serhatuludag.artbookwithfragment.model.Art
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ListFragment : Fragment() {
    private lateinit var binding : FragmentListBinding
    private lateinit var artAdapter : ArtListAdapter
    private val mDisposable = CompositeDisposable()
    private lateinit var artDao : ArtDao
    private lateinit var artDatabase : ArtDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        artDatabase = Room.databaseBuilder(requireContext(), ArtDatabase::class.java, "Arts").build()

        artDao = artDatabase.artDao()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromSQLite()

    }

    private fun getDataFromSQLite() {
        mDisposable.add(artDao.getArtWithNameAndId().subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()).subscribe(this::handleResponse))
    }

    private fun handleResponse(artList: List<Art>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        artAdapter = ArtListAdapter(artList)
        binding.recyclerView.adapter = artAdapter
    }

}