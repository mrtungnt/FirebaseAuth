package com.example.firebaseauth.data.local

import androidx.datastore.core.DataStore
import com.example.firebaseauth.CountriesAndDialCodes
import com.example.firebaseauth.di.CountriesAndDialCodesDataStore
import javax.inject.Inject

class CountriesAndDialCodesLocalSource @Inject constructor(@CountriesAndDialCodesDataStore private val dataStore: DataStore<CountriesAndDialCodes>) {
    fun getData() = dataStore.data

    suspend fun saveCountriesAndDialCodes(countriesAndDialCodes: Collection<CountriesAndDialCodes.CountryAndDialCode>) {
        dataStore.updateData { it.toBuilder().addAllData(countriesAndDialCodes).build() }
    }
}