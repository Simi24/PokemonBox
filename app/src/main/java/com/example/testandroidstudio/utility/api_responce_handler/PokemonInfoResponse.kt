package com.example.testandroidstudio.utility.api_responce_handler

import com.google.gson.annotations.SerializedName

data class PokemonInfoResponse(
    val id: Int,
    val name: String,
    val types: List<TypeSlot>,
    val sprites: Sprites
)

data class TypeSlot(val type: Type)
data class Type(val name: String)
data class Sprites(
    @SerializedName("front_default") val frontDefault: String?,
    @SerializedName("other") val other: Other?
) {
    val officialArtworkFrontDefault: String?
        get() = other?.officialArtwork?.frontDefault
}

data class Other(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtwork?
)

data class OfficialArtwork(
    @SerializedName("front_default") val frontDefault: String?
)

data class PokemonSpeciesResponse(
    val flavor_text_entries: List<FlavorTextEntry>
)

data class FlavorTextEntry(
    val flavor_text: String,
    val language: Language
)

data class Language(val name: String)