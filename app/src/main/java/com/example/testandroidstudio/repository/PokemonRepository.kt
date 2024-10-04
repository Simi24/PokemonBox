package com.example.testandroidstudio.repository

import androidx.lifecycle.MutableLiveData
import com.example.testandroidstudio.model.Pokemon
import com.example.testandroidstudio.utility.api_responce_handler.PokemonInfoResponse
import com.example.testandroidstudio.utility.api_responce_handler.PokemonSpeciesResponse
import com.example.testandroidstudio.network.RetrofitClient
import com.example.testandroidstudio.utility.Constants
import java.util.Locale

class PokemonRepository() {
    private var retrofitInstance = RetrofitClient.getRetrofitInstance()

    suspend fun getPokemonList(ids: MutableLiveData<MutableList<Int>>): List<Pokemon> {
        val remainingIds = ids.value ?: mutableListOf()

        return (1..20).mapNotNull {
            var id: Int
            do {
                id = (1..Constants.TOTAL_POKEMON_COUNT).random()
            } while (!remainingIds.contains(id)) // Avoid duplicates

            ids.value?.remove(id)

            val pokemonInfo = try {
                retrofitInstance.getPokemonInfoById(id).body()
            } catch (e: Exception) {
                throw e
            }

            val pokemonSpecies = try {
                retrofitInstance.getPokemonSpecies(id).body()
            } catch (e: Exception) {
                throw e
            }

            createPokemon(id, pokemonInfo, pokemonSpecies)
        }
    }

    suspend fun searchPokemon(query: String): Pokemon? {
        if (query.isEmpty()) {
            return null
        }

        var pokemonInfo: PokemonInfoResponse? = null
        var pokemonSpecies: PokemonSpeciesResponse? = null
        val id: Int

        try {
            pokemonInfo = retrofitInstance.getPokemonInfoByName(query).body()
            id = pokemonInfo?.id ?: return null
            pokemonSpecies = retrofitInstance.getPokemonSpecies(id).body()
        } catch (e: Exception) {
            throw e
        }

        return createPokemon(id, pokemonInfo, pokemonSpecies)
    }

    private fun createPokemon(id: Int, pokemonInfo: PokemonInfoResponse?, pokemonSpecies: PokemonSpeciesResponse?): Pokemon {
        return Pokemon(
            id = id,
            name = pokemonInfo?.name?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                ?: "",
            types = pokemonInfo?.types?.map { it.type.name.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString() } } ?: emptyList(),
            imageUrl = pokemonInfo?.sprites?.officialArtworkFrontDefault ?: "",
            description = pokemonSpecies?.flavor_text_entries
                ?.firstOrNull { it.language.name == "en" }
                ?.flavor_text?.replace(Regex("""(\r\n)|\n"""), " ") ?: "" // Replace all new lines with spaces
        )
    }
}