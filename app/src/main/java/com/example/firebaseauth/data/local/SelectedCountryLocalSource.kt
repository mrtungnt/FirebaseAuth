package com.example.firebaseauth.data.local

import androidx.datastore.core.DataStore
import com.example.firebaseauth.CountriesAndDialCodes
import com.example.firebaseauth.SelectedCountry
import com.example.firebaseauth.di.SelectedCountryDataStore
import javax.inject.Inject

class SelectedCountryLocalSource @Inject constructor(@SelectedCountryDataStore private val dataStore: DataStore<SelectedCountry>) {
    suspend fun saveSelectedCountry(selectedCountry: CountriesAndDialCodes.CountryAndDialCode) {
        dataStore.updateData {
            it.toBuilder().setCountryAndDialCode(selectedCountry).build()
        }
    }

    fun getFlowOfSelectedCountry() = dataStore.data
}