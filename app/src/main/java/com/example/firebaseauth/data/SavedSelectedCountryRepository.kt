package com.example.firebaseauth.data

import com.example.firebaseauth.CountriesAndDialCodes
import com.example.firebaseauth.data.local.SelectedCountryLocalSource
import javax.inject.Inject

class SavedSelectedCountryRepository @Inject constructor(private val localSource: SelectedCountryLocalSource) {
    suspend fun getSelectedCountry() = localSource.getSelectedCountry()
    suspend fun saveSelectedCountry(selectedCountry: CountriesAndDialCodes.CountryAndDialCode) {
        localSource.dataStore.updateData {
            it.toBuilder().setCountryAndDialCode(selectedCountry).build()
        }
    }
}