package com.example.testandroidstudio.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.testandroidstudio.R
import com.example.testandroidstudio.databinding.FragmentSearchResultBinding
import com.example.testandroidstudio.viewModel.SearchPokemonResultViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchPokemonResultFragment : Fragment() {
    //region Properties
    private lateinit var binding: FragmentSearchResultBinding
    private val viewModel: SearchPokemonResultViewModel by activityViewModels()
    //endregion Properties

    //region Lifecycle Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initializeData(arguments)
        setupUI()
        observeViewModel()
    }
    //endregion Lifecycle Methods

    //region UI Setup
    private fun setupUI() {
        setupGoBackButton()
    }

    private fun setupGoBackButton() {
        binding.goBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_searchResult_to_pokemonListView)
        }
    }
    //endregion UI Setup

    //region ViewModel Observation
    private fun observeViewModel() {
        viewModel.pokemon.observe(viewLifecycleOwner) { pokemonData ->
            updatePokemonName(pokemonData.name)
            updatePokemonImage(pokemonData.imageUrl)
            updatePokemonDescription(pokemonData.description)
            updatePokemonTypes(pokemonData.types)
        }
    }

    private fun updatePokemonName(name: String?) {
        binding.pokemonNameTextView.text = name ?: "Name not available"
    }

    private fun updatePokemonImage(imageUrl: String?) {
        if (imageUrl != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                loadImage(imageUrl, binding.imageView)
            }
        } else {
            binding.imageView.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    private fun updatePokemonDescription(description: String?) {
        binding.pokemonDescription.text = description ?: "Description not available"
    }

    private fun updatePokemonTypes(types: List<String>?) {
        binding.typesChip.removeAllViews()
        types?.forEach { type ->
            binding.typesChip.addView(createTypeChip(type))
        }
    }
    //endregion ViewModel Observation

    //region Helper Methods
    private fun createTypeChip(type: String): Chip {
        return Chip(requireContext()).apply {
            text = type
            setTypeface(null, Typeface.BOLD)
            setChipBackgroundColorResource(R.color.chip_background)
            setTextColor(Color.DKGRAY)
            chipStrokeWidth = 0f
            isClickable = false
        }
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
            Log.e("SearchPokemonResultFragment", "Error loading image: $e")
        }
    }
    //endregion Helper Methods
}