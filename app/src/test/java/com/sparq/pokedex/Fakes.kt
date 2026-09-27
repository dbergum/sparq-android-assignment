package com.sparq.pokedex

import com.sparq.pokedex.data.local.PageKeyEntity
import com.sparq.pokedex.data.local.PokemonDao
import com.sparq.pokedex.data.local.PokemonEntity
import com.sparq.pokedex.data.remote.PokemonApi
import com.sparq.pokedex.data.remote.PokemonDto
import com.sparq.pokedex.data.remote.PokemonPageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class FakePokemonDao : PokemonDao
{
    private val rows = mutableListOf<PokemonEntity>()
    var key: PageKeyEntity? = null

    override suspend fun pageAfter(afterId: Long, limit: Int) = rows.filter { it.id > afterId }.take(limit)
    override suspend fun pageKey() = key
    override suspend fun insertPage(pokemon: List<PokemonEntity>, key: PageKeyEntity)
    {
        rows += pokemon.mapIndexed { i, p -> p.copy(id = rows.size + i + 1L) }
        this.key = key
    }
}

class FakePokemonApi(private val pages: Map<String, PokemonPageDto>) : PokemonApi
{
    val requested = mutableListOf<String>()
    var error: Exception? = null

    override suspend fun page(url: String): PokemonPageDto
    {
        requested += url
        error?.let { throw it }
        return pages.getValue(url)
    }
}

fun page(next: String?, vararg ns: Int) = PokemonPageDto(
    next = next,
    results = ns.map { PokemonDto("mon$it", "https://pokeapi.co/api/v2/pokemon/$it/") },
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher()
{
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
