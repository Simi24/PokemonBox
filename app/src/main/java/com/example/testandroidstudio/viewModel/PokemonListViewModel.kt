package com.example.testandroidstudio.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.testandroidstudio.model.Pokemon
import com.example.testandroidstudio.repository.PokemonRepository
import com.example.testandroidstudio.utility.Constants
import com.example.testandroidstudio.utility.PokemonUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PokemonListViewModel() : ViewModel() {
    private val _pokemonList = MutableLiveData<List<Pokemon>>()
    val pokemonList: LiveData<List<Pokemon>> = _pokemonList

    private val _currentPokemonList = MutableLiveData<List<Pokemon>>()
    val currentPokemonList: LiveData<List<Pokemon>> = _currentPokemonList

    private val _searchResults = MutableLiveData<Pokemon>()
    val searchResults: LiveData<Pokemon> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var _currentPage = MutableLiveData<Int>(0)
    val currentPage: LiveData<Int> = _currentPage

    private var _maxPage = MutableLiveData<Int>(0)
    val maxPage: LiveData<Int> = _maxPage

    private val _pokemonUiState = MutableLiveData<PokemonUiState>(PokemonUiState.Loading())
    val pokemonUiState: LiveData<PokemonUiState> = _pokemonUiState

    // List of Pokemon IDs that have not been extracted yet, used to avoid duplicates
    private val unextractedPokemonIds: MutableLiveData<MutableList<Int>> = MutableLiveData((1..Constants.TOTAL_POKEMON_COUNT).toMutableList())

    private var pokemonRepository = PokemonRepository()

    fun loadPokemonList(): Job {
        return viewModelScope.launch {
            try{
                val list = pokemonRepository.getPokemonList(unextractedPokemonIds)
                _pokemonList.value = list
                updateCurrentList()
            } catch (e: Exception) {
                _pokemonUiState.value = PokemonUiState.Error("Error loading Pokemon list")
            }
        }
    }

    fun loadMorePokemon(): Job {
        return viewModelScope.launch {
            try {
                // Not set to loading state because the user not always is waiting for this data. I don't want to keep the user waiting while they may scroll back the list.
                val newList = pokemonRepository.getPokemonList(unextractedPokemonIds)
                val currentList = _pokemonList.value ?: emptyList()
                _pokemonList.value = currentList + newList
                _pokemonUiState.value = PokemonUiState.Success(_pokemonList.value!!)
            } catch (e: Exception) {
                _pokemonUiState.value = PokemonUiState.Error("Error loading more Pokemon")
            }
        }
    }

    fun searchPokemon(query: String): Job {
        return viewModelScope.launch {
            try {
                _pokemonUiState.value = PokemonUiState.Loading()
                _searchResults.value = pokemonRepository.searchPokemon(query)
                _pokemonUiState.value = _searchResults.value?.let { PokemonUiState.Success(it) }!!
            } catch (e: Exception) {
                _pokemonUiState.value = PokemonUiState.Error("Error searching Pokemon")
            }
        }
    }

     private fun updateCurrentList() {
         val currentList = _pokemonList.value ?: emptyList()
         val start = _currentPage.value?.times(Constants.PAGE_SIZE)
         val end = (_currentPage.value?.plus(1))?.times(Constants.PAGE_SIZE)
         if (start != null && end != null) {
             _currentPokemonList.value = currentList.subList(start, end)
         }
         _pokemonUiState.value = _currentPokemonList.value?.let { PokemonUiState.Success(it) }!!
     }

    fun incrementPage() {
        _currentPage.value = (_currentPage.value ?: 0) + 1

        maxPage.value?.let {
            if ((_currentPage.value ?: 0) > it) {
                _maxPage.value = _currentPage.value
            }
        }
        updateCurrentList()
    }

    fun decrementPage() {
        _currentPage.value = (_currentPage.value ?: 0) - 1
        updateCurrentList()
    }
}
