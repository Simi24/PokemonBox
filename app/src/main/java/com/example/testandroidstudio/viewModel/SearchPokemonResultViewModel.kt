package com.example.testandroidstudio.viewModel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testandroidstudio.model.Pokemon

class SearchPokemonResultViewModel : ViewModel() {
    private val _pokemon = MutableLiveData<Pokemon>()
    val pokemon: LiveData<Pokemon> = _pokemon

    fun initializeData(args: Bundle?) {
        _pokemon.value = Pokemon(
            args?.getInt("id") ?: 0,
            args?.getString("name") ?: "",
            (args?.getStringArray("type") ?: arrayOf()).toList(),
            args?.getString("imageUrl") ?: "",
            args?.getString("description") ?: ""
        )
    }
}
