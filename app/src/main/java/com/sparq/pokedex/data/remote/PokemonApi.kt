package com.sparq.pokedex.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Url

interface PokemonApi
{
    @GET
    suspend fun page(@Url url: String): PokemonPageDto

    companion object
    {
        const val BASE_URL = "https://pokeapi.co/api/v2/"

        const val FIRST_PAGE = "${BASE_URL}pokemon?limit=10&offset=0"
    }
}

@Serializable
data class PokemonPageDto(
    val next: String? = null,
    val results: List<PokemonDto> = emptyList(),
)

@Serializable
data class PokemonDto(val name: String, val url: String)
