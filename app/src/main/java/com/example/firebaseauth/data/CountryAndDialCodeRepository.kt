package com.example.firebaseauth.data

import javax.inject.Inject

class CountryAndDialCodeRepository @Inject constructor(
    private val remoteSource: CountryAndDialCodeRemoteSource
) {
    suspend fun getCountriesAndDialCodes(): Result<CountryAndDialCodeAggregateModel> =
        remoteSource.getCountriesAndDialCodes()
}