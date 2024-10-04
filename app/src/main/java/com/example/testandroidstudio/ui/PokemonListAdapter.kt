package com.example.testandroidstudio.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testandroidstudio.R
import com.example.testandroidstudio.model.Pokemon
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PokemonListAdapter : ListAdapter<Pokemon, PokemonListAdapter.PokemonViewHolder>(PokemonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.pokemonImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.pokemonNameTextView)
        private val typeChipGroup: ChipGroup = itemView.findViewById(R.id.typeChipGroup)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        fun bind(pokemon: Pokemon) {
            nameTextView.text = pokemon.name
            descriptionTextView.text = pokemon.description

            CoroutineScope(Dispatchers.Main).launch {
                if(pokemon.imageUrl != ""){
                    loadImage(pokemon.imageUrl, imageView)
                }
            }

            typeChipGroup.removeAllViews()
            pokemon.types.forEach { type ->
                val chip = Chip(itemView.context)
                chip.text = type
                chip.setTypeface(null, android.graphics.Typeface.BOLD)
                chip.setChipBackgroundColorResource(R.color.chip_background)
                chip.setTextColor(android.graphics.Color.DKGRAY)
                chip.chipStrokeWidth = 0f
                chip.isClickable = false
                typeChipGroup.addView(chip)
            }
        }

        private suspend fun loadImage(url: String, imageView: ImageView) {
            withContext(Dispatchers.IO) {
                try {
                    val bitmap = Glide.with(imageView.context)
                        .asBitmap()
                        .load(url)
                        .submit()
                        .get()
                    withContext(Dispatchers.Main) {
                        imageView.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    println("Error loading image: $e")
                }
            }
        }
    }

    class PokemonDiffCallback : DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem == newItem
        }
    }
}