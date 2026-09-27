package com.sparq.pokedex.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun <T> InfiniteScrollList(
    items: List<T>,
    onLoadMore: () -> Unit,
    canLoadMore: Boolean,
    modifier: Modifier = Modifier,
    loadMoreThreshold: Int = 5,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    key: ((T) -> Any)? = null,
    footer: @Composable () -> Unit = {},
    itemContent: @Composable LazyItemScope.(T) -> Unit,
)
{
    val nearEnd by remember(state, loadMoreThreshold) {
        derivedStateOf {
            val info = state.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= info.totalItemsCount - 1 - loadMoreThreshold
        }
    }

    LaunchedEffect(nearEnd, canLoadMore, items.size) {
        if (nearEnd && canLoadMore) onLoadMore()
    }

    LazyColumn(modifier = modifier, state = state, contentPadding = contentPadding) {
        items(items, key = key) { itemContent(it) }
        item(key = "infinite-scroll-footer") { footer() }
    }
}

@Composable
fun LoadingFooter(modifier: Modifier = Modifier)
{
    Box(modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true, heightDp = 320)
@Composable
private fun InfiniteScrollListPreview()
{
    MaterialTheme {
        InfiniteScrollList(
            items = List(8) { "Item ${it + 1}" },
            onLoadMore = {},
            canLoadMore = true,
            footer = { LoadingFooter() },
        ) { Text(it, Modifier.padding(16.dp)) }
    }
}
