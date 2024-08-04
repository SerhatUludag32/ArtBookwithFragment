package com.serhatuludag.artbookwithfragment.view

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.serhatuludag.artbookwithfragment.R
import com.serhatuludag.artbookwithfragment.database.ArtDao
import com.serhatuludag.artbookwithfragment.database.ArtDatabase
import com.serhatuludag.artbookwithfragment.databinding.FragmentSaveBinding
import com.serhatuludag.artbookwithfragment.model.Art
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.jar.Manifest


class SaveFragment : Fragment() {
    private lateinit var binding: FragmentSaveBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedPicture : Uri? = null
    var selectedBitmap : Bitmap? = null
    private lateinit var artDao : ArtDao
    private lateinit var artDatabase : ArtDatabase
    var artFromMain : Art? = null
    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
        artDatabase = Room.databaseBuilder(requireContext(), ArtDatabase::class.java, "Arts").build()

        artDao = artDatabase.artDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSaveBinding.inflate(inflater, container, false)
        val view = binding.root
        return view


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveButton.setOnClickListener {
            saveButtonClicked()
        }
        binding.deleteButton.setOnClickListener {
            deleteButtonClicked()
        }
        binding.imageView.setOnClickListener {
            imageClicked()
        }

        arguments?.let {
            val info = SaveFragmentArgs.fromBundle(it).info
            if (info=="new"){
                //ADD new art
                binding.artNameText.setText("")
                binding.artistNameText.setText("")
                binding.yearText.setText("")
                binding.saveButton.visibility=View.VISIBLE
                binding.deleteButton.visibility=View.GONE

                val selectedImage = BitmapFactory.decodeResource(context?.resources, R.drawable.selectimage)
                binding.imageView.setImageBitmap(selectedImage)
            }else{
                //Showing old art
                val selectedId = SaveFragmentArgs.fromBundle(it).id
                mDisposable.add(artDao.getArtById(selectedId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponseWithOldArt))

                binding.saveButton.visibility=View.GONE
                binding.deleteButton.visibility=View.VISIBLE

            }
        }
    }

    private fun handleResponseWithOldArt(art : Art) {
        artFromMain = art
        binding.artNameText.setText(art.artName)
        binding.artistNameText.setText(art.artistName)
        binding.yearText.setText(art.year)
        art.image?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            binding.imageView.setImageBitmap(bitmap)
        }

    }

    private fun handleResponse() {
        val action = SaveFragmentDirections.actionSaveFragmentToListFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun saveButtonClicked() {
        val artName = binding.artNameText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()

        if (artName.isBlank()) {
            binding.artNameText.error = "Art name is required"
            return
        }

        if (artistName.isBlank()) {
            binding.artistNameText.error = "Artist name is required"
            return
        }

        if (year.isBlank()) {
            binding.yearText.error = "Year is required"
            return
        }

        // Check if the year is a valid integer
        val yearInt = year.toIntOrNull()
        if (yearInt == null) {
            binding.yearText.error = "Invalid year"
            return
        }

        if (selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)

            val byteArray = outputStream.toByteArray()

            val art = Art(artName,artistName,year,byteArray)

            mDisposable.add(artDao.insert(art).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse))
        }else{
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteButtonClicked() {
        artFromMain?.let {
            mDisposable.add(artDao.delete(it).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse))
        }

    }

    private fun imageClicked() {
            activity?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireActivity().applicationContext, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_MEDIA_IMAGES)) {
                            view?.let { it1 ->
                                Snackbar.make(it1, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                                    View.OnClickListener {
                                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                                    }).show()
                            }
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    } else {
                        val intentToGallery =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        activityResultLauncher.launch(intentToGallery)

                    }
                } else {
                    if (ContextCompat.checkSelfPermission(requireActivity().applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            view?.let { it1 ->
                                Snackbar.make(it1, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                                    View.OnClickListener {
                                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                    }).show()
                            }
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    } else {
                        val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        activityResultLauncher.launch(intentToGallery)
                    }
                }
            }

    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    selectedPicture = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(
                                requireActivity().contentResolver,
                                selectedPicture!!
                            )
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                selectedPicture
                            )
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { result ->
            if (result) {
                //permission granted
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                Toast.makeText(requireContext(), "Permisson needed!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun makeSmallerBitmap(image : Bitmap, maxSize: Int) : Bitmap{
        var width = image.width
        var height = image.height

        var imageRatio : Double = width.toDouble() / height.toDouble()
        if (imageRatio > 1){
            width = maxSize
            val scaledHeight = width / imageRatio
            height = scaledHeight.toInt()
        }else{
            height = maxSize
            val scaledWidth = height * imageRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image, width, height, true)

    }

}