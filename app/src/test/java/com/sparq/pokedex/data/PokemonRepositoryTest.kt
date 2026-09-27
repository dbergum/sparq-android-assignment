package com.sparq.pokedex.data

import com.sparq.pokedex.FakePokemonApi
import com.sparq.pokedex.FakePokemonDao
import com.sparq.pokedex.data.remote.PokemonApi.Companion.FIRST_PAGE
import com.sparq.pokedex.data.remote.PokemonDto
import com.sparq.pokedex.data.remote.PokemonPageDto
import com.sparq.pokedex.page
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonRepositoryTest
{
    private val page2 = "https://pokeapi.co/api/v2/pokemon?offset=2&limit=2"
    private val api = FakePokemonApi(
        mapOf(
            FIRST_PAGE to page(next = page2, 1, 2),
            page2 to page(next = null, 3, 4),
        ),
    )
    private val dao = FakePokemonDao()
    private val repository = PokemonRepository(api, dao)

    @Test
    fun `empty cache fetches the first page and returns it with generated ascending ids`() = runTest {
        val result = repository.pageAfter(0)

        assertEquals(listOf(FIRST_PAGE), api.requested)
        assertEquals(listOf(Pokemon(1L, "mon1"), Pokemon(2L, "mon2")), result)
    }

    @Test
    fun `cached rows are served from Room without a network request`() = runTest {
        repository.pageAfter(0)
        val result = PokemonRepository(api, dao).pageAfter(0)

        assertEquals(listOf("mon1", "mon2"), result.map { it.name })
        assertEquals(1, api.requested.size)
    }

    @Test
    fun `when Room runs out the next url is fetched`() = runTest {
        repository.pageAfter(0)
        val result = repository.pageAfter(2)

        assertEquals(listOf(FIRST_PAGE, page2), api.requested)
        assertEquals(listOf("mon3", "mon4"), result.map { it.name })
    }

    @Test
    fun `once the end is reached an empty list is returned without further network calls`() = runTest {
        repository.pageAfter(0)
        repository.pageAfter(2)
        val result = repository.pageAfter(4)

        assertTrue(result.isEmpty())
        assertEquals(2, api.requested.size)
    }

    @Test
    fun `duplicate names are stored as separate rows`() = runTest {
        val dupes = PokemonPageDto(next = null, results = List(2) { PokemonDto("pikachu", "https://pokeapi.co/api/v2/pokemon/25/") })
        val repo = PokemonRepository(FakePokemonApi(mapOf(FIRST_PAGE to dupes)), FakePokemonDao())

        assertEquals(listOf(Pokemon(1L, "pikachu"), Pokemon(2L, "pikachu")), repo.pageAfter(0))
    }
}
