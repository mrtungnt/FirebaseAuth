package com.example.firebaseauth.data.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.firebaseauth.data.CountryNamesAndCallingCodesModel
import javax.inject.Inject

class CountryNamesAndCallingCodesRepository @Inject constructor(private val countryNamesAndCallingCodesService: CountryNamesAndCallingCodesService) :
    PagingSource<Int, CountryNamesAndCallingCodesModel>() {

    /**
     * Loading API for [PagingSource].
     *
     * Implement this method to trigger your async load (e.g. from database or network).
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CountryNamesAndCallingCodesModel> {
        return try {
            val pageNumber = params.key ?: 0

            val prevKey = if (pageNumber > 0) pageNumber - 1 else null

            val data = countryNamesAndCallingCodesService.getCountryNamesAndCallingCodesInPages(
                pageNumber
            )

            // This API defines that it's out of data when a page returns empty. When out of
            // data, we return `null` to signify no more pages should be loaded
            val nextKey = if (data.isNotEmpty()) pageNumber + 1 else null

            LoadResult.Page(
                data = data,
                prevKey = prevKey, nextKey = nextKey
            )
        } catch (exc: Exception) {
            LoadResult.Error(exc)
        }
    }

    /**
     * Provide a [Key] used for the initial [load] for the next [PagingSource] due to invalidation
     * of this [PagingSource]. The [Key] is provided to [load] via [LoadParams.key].
     *
     * The [Key] returned by this method should cause [load] to load enough items to
     * fill the viewport *around* the last accessed position, allowing the next generation to
     * transparently animate in. The last accessed position can be retrieved via
     * [state.anchorPosition][PagingState.anchorPosition], which is typically
     * the *top-most* or *bottom-most* item in the viewport due to access being triggered by binding
     * items as they scroll into view.
     *
     * For example, if items are loaded based on integer position keys, you can return
     * `( (state.anchorPosition ?: 0) - state.config.initialLoadSize / 2).coerceAtLeast(0)`.
     *
     * Alternately, if items contain a key used to load, get the key from the item in the page at
     * index [state.anchorPosition][PagingState.anchorPosition] then try to center it based on
     * `state.config.initialLoadSize`.
     *
     * @param state [PagingState] of the currently fetched data, which includes the most recently
     * accessed position in the list via [PagingState.anchorPosition].
     *
     * @return [Key] passed to [load] after invalidation used for initial load of the next
     * generation. The [Key] returned by [getRefreshKey] should load pages centered around
     * user's current viewport. If the correct [Key] cannot be determined, `null` can be returned
     * to allow [load] decide what default key to use.
     */
    override fun getRefreshKey(state: PagingState<Int, CountryNamesAndCallingCodesModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    val pageSize = countryNamesAndCallingCodesService.pageSize

    suspend fun getAllCountryNamesAndCallingCodes() =
        countryNamesAndCallingCodesService.searchCountryNamesAndCallingCodes()
}