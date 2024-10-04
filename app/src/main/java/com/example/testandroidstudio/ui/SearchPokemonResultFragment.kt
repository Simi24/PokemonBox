package com.example.testandroidstudio.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.testandroidstudio.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchPokemonResultFragment : Fragment() {
    private var pokemonName: String? = null
    private var pokemonImageUrl: String? = null
    private var pokemonDescription: String? = null
    private var pokemonTypes: Array<String>? = null

    private lateinit var nameTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var descriptionTextView: TextView
    private lateinit var typeChipGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pokemonName = it.getString("name")
            pokemonImageUrl = it.getString("imageUrl")
            pokemonDescription = it.getString("description")
            pokemonTypes = it.getStringArray("type")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_result, container, false)

        nameTextView = view.findViewById(R.id.pokemonNameTextView)
        imageView = view.findViewById(R.id.imageView)
        descriptionTextView = view.findViewById(R.id.pokemonDescription)
        typeChipGroup = view.findViewById(R.id.typesChip)

        nameTextView.text = pokemonName ?: "Name not available"

        if (pokemonImageUrl != null) {
            CoroutineScope(Dispatchers.Main).launch {
                loadImage(pokemonImageUrl!!, imageView)
            }
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground)
        }

        descriptionTextView.text = pokemonDescription ?: "Description not available"

        typeChipGroup.removeAllViews()
        pokemonTypes?.forEach { type ->
            val chip = Chip(requireContext())
            chip.text = type
            chip.setTypeface(null, android.graphics.Typeface.BOLD)
            chip.setChipBackgroundColorResource(R.color.chip_background)
            chip.setTextColor(android.graphics.Color.DKGRAY)
            chip.chipStrokeWidth = 0f
            chip.isClickable = false
            typeChipGroup.addView(chip)
        }

        view.findViewById<Button>(R.id.goBackButton).setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_searchResult_to_pokemonListView)
        }

        return view
    }

    private suspend fun loadImage(url: String, imageView: ImageView) {
        try {
            withContext(Dispatchers.IO) {
                val bitmap = Glide.with(imageView.context)
                    .asBitmap()
                    .load(url)
                    .submit()
                    .get()
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        } catch (e: Exception) {
            println("Error loading image: $e")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchPokemonResultFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
