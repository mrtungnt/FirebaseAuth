package com.example.firebaseauth.data.local

import androidx.datastore.core.DataStore
import com.example.firebaseauth.CountryNamesAndDialCodes
import com.example.firebaseauth.di.CountryNamesAndDialCodesDataStore
import javax.inject.Inject

class CountryNamesAndDialCodesLocalSource @Inject constructor(@CountryNamesAndDialCodesDataStore private val dataStore: DataStore<CountryNamesAndDialCodes>) {
    fun getData() = dataStore.data

    suspend fun saveCountriesAndDialCodes(countryNamesAndDialCodes: Collection<CountryNamesAndDialCodes.NameAndDialCode>) {
        dataStore.updateData { it.toBuilder().addAllNamesAndDialCodes(countryNamesAndDialCodes).build() }
    }
}