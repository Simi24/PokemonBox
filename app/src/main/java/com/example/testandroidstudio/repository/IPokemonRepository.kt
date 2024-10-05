package com.example.testandroidstudio.repository

import com.example.testandroidstudio.model.Pokemon

interface IPokemonRepository {
    suspend fun getPokemonList(ids: MutableList<Int>): List<Pokemon>
    suspend fun searchPokemon(query: String): Pokemon?
}