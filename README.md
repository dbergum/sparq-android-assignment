# Pokédex – InfiniteScrollList

An infinite-scrolling list of Pokémon names from [PokeAPI](https://pokeapi.co). It follows the
response's `next` URL to load pages in the background as you scroll.

## Build & run
- Open in Android Studio (JDK 17+) and run the `app` configuration, **or**
- `./gradlew installDebug`
- Unit tests: `./gradlew testDebugUnitTest`
- Compose UI test (needs an emulator or device): `./gradlew connectedDebugAndroidTest`

## Architecture (MVVM + repository, single source of truth)

```
PokemonScreen ──collect──▶ PokemonViewModel ──pageAfter(lastId)──▶ PokemonRepository
      │                                                             │ 1. read Room
      └─ InfiniteScrollList                                         │ 2. empty? fetch `next` from PokemonApi,
         (generic, reusable)                                        │    save to Room, read Room again
                                                                    ▼
                                                              Room (PokemonDao)
```

| Layer | File | Responsibility |
|---|---|---|
| Component | `ui/components/InfiniteScrollList.kt` | Generic `LazyColumn<T>` that calls `onLoadMore` near the end. Knows nothing about Pokémon. |
| UI | `ui/pokemon/PokemonScreen.kt` | Stateful wrapper around a stateless `PokemonContent`. Footer shows loading, error + retry, or end of list. |
| ViewModel | `ui/pokemon/PokemonViewModel.kt` | Holds the loaded list and paging state (`isLoading`, `error`, `endReached`) in one `StateFlow`. |
| Repository | `data/PokemonRepository.kt` | Serves pages from Room; fetches from the network only when Room returns nothing. A `Mutex` stops duplicate fetches. |
| Cache | `data/local/PokemonDatabase.kt` | `pokemon` table plus a one-row `page_key` table that stores the next URL. |
| DI | `di/AppModule.kt` | Hilt provides Retrofit and Room. Everything else uses `@Inject` constructors. |

## InfiniteScrollList
```kotlin
InfiniteScrollList(
    items = state.items,
    onLoadMore = viewModel::loadMore,
    canLoadMore = state.canLoadMore,   // false while loading, after an error, or at the end
    loadMoreThreshold = 5,             // prefetch 5 items before the bottom
    key = { it.id },
    footer = { if (state.isLoading) LoadingFooter() },
) { item -> Text(item.name) }
```
- **Scroll detection:** `derivedStateOf` over `LazyListState.layoutInfo`. It recomposes only when
  the "near end" flag changes, not on every scroll frame.
- **Trigger:** `LaunchedEffect(nearEnd, canLoadMore, items.size)` calls `onLoadMore` when both are true, and
  checks again after every appended page. If a page doesn't fill the screen, the next one loads automatically. The first page also loads
  on its own, because an empty list counts as "near the end".
- **No retry storms:** after a failure the caller sets `canLoadMore = false` until the user taps Retry.

## Caching strategy
- **Room is the single source of truth.** Every page is read from Room first. The network is used **only
  when Room returns an empty result**, i.e. the user has scrolled past everything cached.
- Network results are written to Room and then read back from Room, so the UI only ever shows stored data.
- Each page's items and its `next` URL are saved in **one transaction**, so the cache and the paging
  cursor always match.
- **Survives process death and works offline:** on relaunch, pages are served from Room with no network
  calls until the cache runs out; then paging continues from the stored `next` URL.
- Once `next` is null, the end is stored and no more network calls are made.
- **Config changes:** the ViewModel keeps the loaded list and any running request alive through rotation.

## Tests
- `PokemonRepositoryTest`: first page, serving cached rows without the network, following `next`, stopping at the end, duplicate names.
- `PokemonViewModelTest`: appending, warm-cache relaunch with no network, no-op at the end, error state and retry.
- `InfiniteScrollListTest` (instrumented): the component loads only when scrolled near the end.

Tests use hand-written fakes for the API and DAO; no mocking library is needed.

## Trade-offs / what I'd do next
- **Why not Paging 3?** The assignment is about building the paging behavior yourself. In production I'd use
  Paging 3 with a `RemoteMediator`, which is essentially this design (Room + remote keys).
- No cache expiry: data is treated as static, which suits PokeAPI. Next steps would be a TTL or pull-to-refresh
  that clears the tables.
- Could add: Pokémon sprites via Coil, a detail screen, and a Robolectric setup so the UI test runs on the JVM.
