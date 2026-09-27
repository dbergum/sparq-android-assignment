package com.sparq.pokedex.ui.pokemon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparq.pokedex.data.Pokemon
import com.sparq.pokedex.data.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PokemonUiState(
    val pokemon: List<Pokemon> = emptyList(),
    val isLoading: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null,
)
{
    val canLoadMore: Boolean get() = !isLoading && !endReached && error == null
}

@HiltViewModel
class PokemonViewModel @Inject constructor(
    private val repository: PokemonRepository,
) : ViewModel()
{
    private val _uiState = MutableStateFlow(PokemonUiState())
    val uiState: StateFlow<PokemonUiState> = _uiState.asStateFlow()

    fun loadMore()
    {
        if (!_uiState.value.canLoadMore) return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try
            {
                val next = repository.pageAfter(_uiState.value.pokemon.lastOrNull()?.id ?: 0)
                _uiState.update {
                    it.copy(pokemon = it.pokemon + next, isLoading = false, endReached = next.isEmpty())
                }
            }
            catch (e: CancellationException)
            {
                throw e
            }
            catch (e: Exception)
            {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Something went wrong") }
            }
        }
    }

    fun retry()
    {
        _uiState.update { it.copy(error = null) }
        loadMore()
    }
}
