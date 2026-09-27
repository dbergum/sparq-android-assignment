package com.sparq.pokedex.ui.pokemon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparq.pokedex.data.Pokemon
import com.sparq.pokedex.ui.components.InfiniteScrollList
import com.sparq.pokedex.ui.components.LoadingFooter

@Composable
fun PokemonScreen(viewModel: PokemonViewModel = hiltViewModel())
{
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PokemonContent(state, onLoadMore = viewModel::loadMore, onRetry = viewModel::retry)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonContent(state: PokemonUiState, onLoadMore: () -> Unit, onRetry: () -> Unit)
{
    Scaffold(topBar = {
        TopAppBar(title = {
            Text("Pokédex")
        })
    }) { padding ->
        InfiniteScrollList(
            items = state.pokemon,
            onLoadMore = onLoadMore,
            canLoadMore = state.canLoadMore,
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
            key = Pokemon::id,
            footer = { Footer(state, onRetry) },
        ) { pokemon ->
            ListItem(headlineContent = { Text(pokemon.name.replaceFirstChar(Char::uppercase)) })
            HorizontalDivider()
        }
    }
}

@Composable
private fun Footer(state: PokemonUiState, onRetry: () -> Unit) = when
{
    state.error != null -> Column(
        Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Couldn't load more: ${state.error}", color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry) { Text("Retry") }
    }
    state.isLoading -> LoadingFooter()
    state.endReached -> Text(
        "You've caught them all!",
        Modifier.fillMaxWidth().padding(16.dp),
        style = MaterialTheme.typography.bodyMedium,
    )
    else -> Unit
}

private class PokemonUiStateProvider : PreviewParameterProvider<PokemonUiState>
{
    private val sample = listOf("bulbasaur", "ivysaur", "venusaur", "charmander", "charmeleon")
        .mapIndexed { i, name -> Pokemon(i + 1L, name) }

    override val values = sequenceOf(
        PokemonUiState(pokemon = sample, isLoading = true),
        PokemonUiState(pokemon = sample, error = "Unable to resolve host \"pokeapi.co\""),
        PokemonUiState(pokemon = sample, endReached = true),
        PokemonUiState(error = "Unable to resolve host \"pokeapi.co\""),
    )
}

@Preview(showBackground = true)
@Composable
private fun PokemonContentPreview(@PreviewParameter(PokemonUiStateProvider::class) state: PokemonUiState)
{
    MaterialTheme { PokemonContent(state, onLoadMore = {}, onRetry = {}) }
}
