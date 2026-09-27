package com.sparq.pokedex.ui.pokemon

import com.sparq.pokedex.FakePokemonApi
import com.sparq.pokedex.FakePokemonDao
import com.sparq.pokedex.MainDispatcherRule
import com.sparq.pokedex.data.PokemonRepository
import com.sparq.pokedex.data.remote.PokemonApi.Companion.FIRST_PAGE
import com.sparq.pokedex.page
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class PokemonViewModelTest
{
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val page2 = "https://pokeapi.co/api/v2/pokemon?offset=2&limit=2"
    private val api = FakePokemonApi(
        mapOf(
            FIRST_PAGE to page(next = page2, 1, 2),
            page2 to page(next = null, 3, 4),
        ),
    )
    private val dao = FakePokemonDao()

    private fun viewModel() = PokemonViewModel(PokemonRepository(api, dao))

    @Test
    fun `loadMore appends pages until an empty result marks the end`() = runTest {
        val vm = viewModel()

        vm.loadMore()
        assertEquals(listOf("mon1", "mon2"), vm.uiState.value.pokemon.map { it.name })

        vm.loadMore()
        assertEquals(listOf("mon1", "mon2", "mon3", "mon4"), vm.uiState.value.pokemon.map { it.name })

        vm.loadMore()
        assertTrue(vm.uiState.value.endReached)
        assertFalse(vm.uiState.value.canLoadMore)
    }

    @Test
    fun `a relaunch with a warm cache shows Room data without a network request`() = runTest {
        viewModel().loadMore()
        val relaunched = viewModel()
        relaunched.loadMore()

        assertEquals(listOf("mon1", "mon2"), relaunched.uiState.value.pokemon.map { it.name })
        assertEquals(1, api.requested.size)
    }

    @Test
    fun `loadMore is a no-op once the end is reached`() = runTest {
        val vm = viewModel()
        repeat(5) { vm.loadMore() }

        assertEquals(2, api.requested.size)
    }

    @Test
    fun `an error is exposed and pauses auto-loading until retry`() = runTest {
        val vm = viewModel()
        api.error = IOException("offline")
        vm.loadMore()
        assertEquals("offline", vm.uiState.value.error)
        assertFalse(vm.uiState.value.canLoadMore)

        vm.loadMore()
        assertEquals(1, api.requested.size)

        api.error = null
        vm.retry()
        assertNull(vm.uiState.value.error)
        assertEquals(listOf("mon1", "mon2"), vm.uiState.value.pokemon.map { it.name })
    }
}
