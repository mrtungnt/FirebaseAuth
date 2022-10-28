/*
package com.example.firebaseauth.data.local

import androidx.datastore.core.DataStore
import com.example.firebaseauth.CountryNameAndCallingCode
import com.example.firebaseauth.CountryNamesAndCallingCodesCollection
import com.example.firebaseauth.di.CountryNamesAndCallingCodesDataStore
import javax.inject.Inject

class CountryNamesAndDialCodesSource @Inject constructor(@CountryNamesAndCallingCodesDataStore private val dataStore: DataStore<CountryNamesAndCallingCodesCollection>) {
    fun getData() = dataStore.data

    suspend fun saveCountriesAndDialCodes(countryNamesAndCallingCodes: Collection<CountryNameAndCallingCode>) {
        dataStore.updateData { it.toBuilder().addAllEntries(countryNamesAndCallingCodes).build() }
    }
}
*/
