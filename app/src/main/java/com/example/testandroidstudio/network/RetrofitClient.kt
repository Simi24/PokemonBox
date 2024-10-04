package com.example.testandroidstudio.network

import com.example.testandroidstudio.model.PokemonApiService
import com.example.testandroidstudio.utility.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    companion object {
        fun getRetrofitInstance(): PokemonApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(PokemonApiService::class.java)
        }
    }
}