package com.example.testandroidstudio.utility

sealed interface PokemonUiState {
    data class Success(val data: Any) : PokemonUiState
    data class LoadingFirstPage(val isLoading: Boolean = true) : PokemonUiState
    data class Error(val messageId: Int) : PokemonUiState
}