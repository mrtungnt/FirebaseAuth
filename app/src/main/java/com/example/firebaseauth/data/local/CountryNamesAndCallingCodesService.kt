package com.example.firebaseauth.data.local

import android.content.Context
import com.example.firebaseauth.R
import com.example.firebaseauth.data.CountryNamesAndCallingCodesModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

class CountryNamesAndCallingCodesService @Inject constructor(val context: Context) {
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
                            Timber.d("${(pageNumber * pageSize + pageSize).coerceAtMost(it)}")
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

    suspend fun searchCountryNamesAndCallingCodes(keyword: String) =
        withContext(context = Dispatchers.Default) {
            try {
                if (countryNamesAndCallingCodes.isEmpty())
                    countryNamesAndCallingCodes =
                        getCountryNamesAndCallingCodesFromJson(context.resources.getStringArray(R.array.countries))

                if (countryNamesAndCallingCodes.isNotEmpty()) {
                    return@withContext countryNamesAndCallingCodes.filter {
                        it.name.contains(keyword, ignoreCase = true) || it.alpha2Code.contains(
                            keyword,
                            ignoreCase = true
                        ) || it.callingCodes.any { cC -> cC.contains(keyword, ignoreCase = true) }
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
