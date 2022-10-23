package com.example.firebaseauth.data.local

import android.content.Context
import com.example.firebaseauth.R
import com.example.firebaseauth.data.CountryNamesAndCallingCodesModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

class CountryNamesAndCallingCodesSource @Inject constructor(val context: Context) {
    val pageSize = 100

    private var countryNamesAndCallingCodes: List<CountryNamesAndCallingCodesModel> = emptyList()

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

            return@withContext countryNamesAndCallingCodes.flatMap { it.toList() }

        } catch (exc: Exception) {
            Timber.e(exc.message)
            throw exc
        }
    }

    suspend fun getCountryNamesAndCallingCodesInPages(pageNumber: Int) =
        withContext(context = Dispatchers.Default) {
            try {
                if (countryNamesAndCallingCodes.isEmpty())
                    countryNamesAndCallingCodes =
                        getCountryNamesAndCallingCodesFromJson(context.resources.getStringArray(R.array.countries))

                if (countryNamesAndCallingCodes.isNotEmpty()) {
                    return@withContext countryNamesAndCallingCodes.count()
                        .let { (pageNumber * pageSize + pageSize).coerceAtMost(it) }.let {
                            countryNamesAndCallingCodes.subList(
                                pageNumber * pageSize,
                                it
                            )
                        }
                } else {
                    throw Exception("Empty list.")
                }
            } catch (exc: Exception) {
                Timber.e(exc.message)
                throw exc
            }
        }
}