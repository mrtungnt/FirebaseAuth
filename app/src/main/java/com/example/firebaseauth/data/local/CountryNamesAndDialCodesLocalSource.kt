package com.example.firebaseauth.data.local

import androidx.datastore.core.DataStore
import com.example.firebaseauth.CountryNamesAndDialCodes
import com.example.firebaseauth.di.CountriesAndDialCodesDataStore
import javax.inject.Inject

class CountryNamesAndDialCodesLocalSource @Inject constructor(@CountriesAndDialCodesDataStore private val dataStore: DataStore<CountryNamesAndDialCodes>) {
    fun getData() = dataStore.data

    suspend fun saveCountriesAndDialCodes(countryNamesAndDialCodes: Collection<CountryNamesAndDialCodes.NameAndDialCode>) {
        dataStore.updateData { it.toBuilder().addAllNamesAndDialCodes(countryNamesAndDialCodes).build() }
    }
}