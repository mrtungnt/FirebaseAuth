package com.example.firebaseauth.data

import com.example.firebaseauth.CountryNamesAndDialCodes
import com.example.firebaseauth.data.local.SelectedCountryLocalSource
import javax.inject.Inject

class SavedSelectedCountryRepository @Inject constructor(private val localSource: SelectedCountryLocalSource) {
    suspend fun saveSelectedCountry(selectedCountry: CountryNamesAndDialCodes.NameAndDialCode) {
        localSource.saveSelectedCountry(selectedCountry)
    }

    fun getFlowOfSelectedCountry() = localSource.getFlowOfSelectedCountry()
}