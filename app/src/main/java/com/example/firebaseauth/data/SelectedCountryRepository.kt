package com.example.firebaseauth.data

import com.example.firebaseauth.CountriesAndDialCodes
import com.example.firebaseauth.data.local.SelectedCountryLocalSource
import javax.inject.Inject

class SelectedCountryRepository @Inject constructor(private val localSource: SelectedCountryLocalSource) {
    suspend fun getSelectedCountry() = localSource.getSelectedCountry()
    fun getData() = localSource.getFlowOfSelectedCountry()
    suspend fun updateSelectedCountry(selectedCountry: CountriesAndDialCodes.CountryAndDialCode) {
        localSource.dataStore.updateData {
            it.toBuilder().setSelectedCountry(selectedCountry).build()
        }
    }
}