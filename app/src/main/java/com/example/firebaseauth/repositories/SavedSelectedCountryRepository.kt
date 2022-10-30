package com.example.firebaseauth.repositories

import com.example.firebaseauth.services.CountryNamesAndCallingCodeModel
import com.example.firebaseauth.services.SavedSelectedCountryService
import javax.inject.Inject

interface SavedSelectedCountryRepository {
    suspend fun saveSelectedCountry(selectedCountry: CountryNamesAndCallingCodeModel)
    suspend fun getSelectedCountry(): CountryNamesAndCallingCodeModel?
}

class SavedSelectedCountryRepositoryImpl @Inject constructor(private val source: SavedSelectedCountryService) :
    SavedSelectedCountryRepository {
    override suspend fun saveSelectedCountry(selectedCountry: CountryNamesAndCallingCodeModel) {
        source.saveSelectedCountry(selectedCountry)
    }

    override suspend fun getSelectedCountry(): CountryNamesAndCallingCodeModel? =
        source.getSelectedCountry()
}
