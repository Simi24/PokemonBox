package com.example.testandroidstudio.network

import com.example.testandroidstudio.model.PokemonApiService
import com.example.testandroidstudio.utility.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: PokemonApiService by lazy {
        retrofit.create(PokemonApiService::class.java)
    }
}
