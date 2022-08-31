package com.example.firebaseauth.data

import com.example.firebaseauth.data.network.CountryAndDialCodeAggregateModel
import com.example.firebaseauth.data.network.CountryAndDialCodeRemoteSource
import javax.inject.Inject

class CountryAndDialCodeRepository @Inject constructor(
    private val remoteSource: CountryAndDialCodeRemoteSource
) {
    suspend fun getCountriesAndDialCodes(): Result<CountryAndDialCodeAggregateModel> =
        remoteSource.getCountriesAndDialCodes()
}