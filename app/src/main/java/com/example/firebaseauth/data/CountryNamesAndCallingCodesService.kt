package com.example.firebaseauth.data

import android.content.Context
import com.example.firebaseauth.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

class CountryNamesAndCallingCodesService @Inject constructor(
    val context: Context,
) {
    val pageSize = 100

    private var countryNamesAndCallingCodesFromJSON: List<CountryNameAndCallingCodeModelFromJSON> =
        mutableListOf()

    private suspend fun getCountryNamesAndCallingCodes(): List<CountryNameAndCallingCodeModelFromJSON> =
        withContext(Dispatchers.Default) {
            when {
                countryNamesAndCallingCodesFromJSON.isEmpty() -> {
                    try {
                        val listOfCountryNamesAndCallingCodesFromJSON =
                            mutableListOf<List<CountryNameAndCallingCodeModelFromJSON>>()

                        val countryNamesAndCallingCodesInJsonStringsList =
                            context.resources.getStringArray(
                                R.array.countries
                            )

                        val json = Json { ignoreUnknownKeys = true }
                        countryNamesAndCallingCodesInJsonStringsList.forEach {
                            listOfCountryNamesAndCallingCodesFromJSON.add(
                                json.decodeFromString<List<CountryNameAndCallingCodeModelFromJSON>>(
                                    it
                                )
                            )
                        }

                        listOfCountryNamesAndCallingCodesFromJSON.flatten()
                    } catch (exc: Exception) {
                        Timber.e(exc.message)
                        throw exc
                    }
                }
                else -> {
                    countryNamesAndCallingCodesFromJSON
                }

            }
        }

    suspend fun getCountryNamesAndCallingCodesInPages(pageNumber: Int): List<CountryNameAndCallingCodeModelFromJSON> =
        withContext(context = Dispatchers.Default) {
            try {
                val countryNamesAndCallingCodes = getCountryNamesAndCallingCodes()

                countryNamesAndCallingCodes.count()
                    .let { (pageNumber * pageSize + pageSize).coerceAtMost(it) }.let {
                        Timber.d("${(pageNumber * pageSize + pageSize).coerceAtMost(it)}")
                        countryNamesAndCallingCodes.subList(
                            pageNumber * pageSize,
                            it
                        )
                    }
            } catch (exc: Exception) {
                Timber.e(exc.message)
                throw exc
            }
        }

    suspend fun searchCountryNamesAndCallingCodes(keyword: String): List<CountryNameAndCallingCodeModelFromJSON> =
        withContext(context = Dispatchers.Default) {
            try {
                val countryNamesAndCallingCodes = getCountryNamesAndCallingCodes()

                countryNamesAndCallingCodes.filter {
                    it.name.contains(keyword, ignoreCase = true) || it.alpha2Code.contains(
                        keyword,
                        ignoreCase = true
                    ) || it.callingCodes.any { callingCode ->
                        callingCode.contains(
                            keyword,
                            ignoreCase = true
                        )
                    }
                }
            } catch (exc: Exception) {
                Timber.e(exc.message)
                throw exc
            }
        }
}
