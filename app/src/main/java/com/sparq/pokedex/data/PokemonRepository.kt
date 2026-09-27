package com.sparq.pokedex.data

import com.sparq.pokedex.data.local.PageKeyEntity
import com.sparq.pokedex.data.local.PokemonDao
import com.sparq.pokedex.data.local.PokemonEntity
import com.sparq.pokedex.data.remote.PokemonApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

data class Pokemon(val id: Long, val name: String)

@Singleton
class PokemonRepository @Inject constructor(
    private val api: PokemonApi,
    private val dao: PokemonDao,
)
{
    private val mutex = Mutex()

    suspend fun pageAfter(afterId: Long): List<Pokemon> = mutex.withLock {
        dao.pageAfter(afterId, PAGE_SIZE)
            .ifEmpty { fetchNextPage(); dao.pageAfter(afterId, PAGE_SIZE) }
            .map { Pokemon(it.id, it.name) }
    }

    private suspend fun fetchNextPage()
    {
        val key = dao.pageKey()
        if (key != null && key.nextUrl == null) return

        val page = api.page(key?.nextUrl ?: PokemonApi.FIRST_PAGE)
        dao.insertPage(
            pokemon = page.results.map { PokemonEntity(name = it.name) },
            key = PageKeyEntity(nextUrl = page.next),
        )
    }

    companion object
    {
        const val PAGE_SIZE = 10
    }
}
