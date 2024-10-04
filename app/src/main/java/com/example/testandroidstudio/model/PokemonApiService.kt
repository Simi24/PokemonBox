package com.example.testandroidstudio.model

import com.example.testandroidstudio.utility.api_responce_handler.PokemonInfoResponse
import com.example.testandroidstudio.utility.api_responce_handler.PokemonSpeciesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PokemonApiService {

    @GET("pokemon/{id}")
    suspend fun getPokemonInfoById(@Path("id") id: Int): Response<PokemonInfoResponse>

    @GET("pokemon/{name}")
    suspend fun getPokemonInfoByName(@Path("name") name: String): Response<PokemonInfoResponse>

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): Response<PokemonSpeciesResponse>
}