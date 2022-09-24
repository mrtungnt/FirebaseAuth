package com.example.firebaseauth.data.local

import androidx.datastore.core.DataStore
import com.example.firebaseauth.CountryNamesAndDialCodes
import com.example.firebaseauth.SelectedCountry
import com.example.firebaseauth.di.SelectedCountryDataStore
import javax.inject.Inject

class SelectedCountryLocalSource @Inject constructor(@SelectedCountryDataStore private val dataStore: DataStore<SelectedCountry>) {
    suspend fun saveSelectedCountry(selectedCountry: CountryNamesAndDialCodes.NameAndDialCode) {
        dataStore.updateData {
            it.toBuilder().setContainer(selectedCountry).build()
        }
    }

    fun getFlowOfSelectedCountry() = dataStore.data
}