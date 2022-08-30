package com.example.firebaseauth.data

import javax.inject.Inject

class CountryRepository @Inject constructor(
    private val remoteSource: RemoteSource
) {
    suspend fun getCountriesAndDialCodes(): Result<CountryListModel> =
        remoteSource.getCountriesAndDialCodes()
}