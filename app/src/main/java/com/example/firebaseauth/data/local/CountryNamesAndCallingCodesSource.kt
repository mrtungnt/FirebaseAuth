package com.example.firebaseauth.data.local

import android.content.Context
import com.example.firebaseauth.R
import com.example.firebaseauth.data.CountryNamesAndCallingCodesModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber

class CountryNamesAndCallingCodesSource(@ApplicationContext context: Context) {
    val PageSize = 100

    private lateinit var countryNamesAndCallingCodes: List<CountryNamesAndCallingCodesModel>

    init {
        CoroutineScope(Dispatchers.Default).launch {
            countryNamesAndCallingCodes =
                getCountryNamesAndCallingCodesFromJson(context.resources.getStringArray(R.array.countries))
        }
    }

    private suspend fun getCountryNamesAndCallingCodesFromJson(
        countryNamesAndCallingCodesInJsonStringsList: Array<String>
    ) = withContext(Dispatchers.Default) {
        val countryNamesAndCallingCodes =
            mutableListOf<List<CountryNamesAndCallingCodesModel>>()

        try {
            val json = Json { ignoreUnknownKeys = true }

            countryNamesAndCallingCodesInJsonStringsList.forEach {
                countryNamesAndCallingCodes.add(
                    json.decodeFromString<List<CountryNamesAndCallingCodesModel>>(
                        it
                    )
                )
            }
        } catch (exc: Exception) {
            Timber.e(exc.message)
        }

        return@withContext countryNamesAndCallingCodes.flatMap { it.toList() }
    }

    suspend fun getCountryNamesAndCallingCodesInPages(pageNumber: Int) =
        withContext(context = Dispatchers.Default) {
            try {
                if (countryNamesAndCallingCodes.isNotEmpty()) {
                    return@withContext countryNamesAndCallingCodes.subList(
                        pageNumber * PageSize,
                        (pageNumber * PageSize + PageSize).coerceAtMost(countryNamesAndCallingCodes.count())
                    )
                } else {
                    throw Exception("Empty list.")
                }
            } catch (exc: Exception) {
                Timber.e(exc.message)
            }

            return@withContext emptyList()
        }
}