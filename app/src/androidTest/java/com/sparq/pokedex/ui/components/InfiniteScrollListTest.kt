package com.sparq.pokedex.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InfiniteScrollListTest
{
    @get:Rule val compose = createComposeRule()

    @Test
    fun loadsMoreOnlyWhenScrolledNearTheEnd()
    {
        var items by mutableStateOf((1..20).toList())
        var loads = 0

        compose.setContent {
            InfiniteScrollList(
                items = items,
                onLoadMore = { loads++; items = items + (items.size + 1..items.size + 20) },
                canLoadMore = true,
                modifier = Modifier.height(300.dp).testTag("list"),
                loadMoreThreshold = 3,
            ) { Text("Item $it", Modifier.height(50.dp)) }
        }

        compose.waitForIdle()
        assertEquals(0, loads)

        compose.onNodeWithTag("list").performScrollToIndex(19)
        compose.waitForIdle()
        assertEquals(1, loads)
        assertEquals(40, items.size)
    }
}
