package com.example.firebaseauth.data

import com.example.firebaseauth.data.local.SelectedCountryService
import javax.inject.Inject

interface SavedSelectedCountryRepository {
    suspend fun saveSelectedCountry(selectedCountry: CountryNameAndCallingCodeModelFromJSON)
    suspend fun getSelectedCountry(): CountryNameAndCallingCodeModelFromJSON
}

class SavedSelectedCountryRepositoryImpl @Inject constructor(private val source: SelectedCountryService) :
    SavedSelectedCountryRepository {
    override suspend fun saveSelectedCountry(selectedCountry: CountryNameAndCallingCodeModelFromJSON) {
        source.saveSelectedCountry(selectedCountry)
    }

    override suspend fun getSelectedCountry(): CountryNameAndCallingCodeModelFromJSON =
        source.getSelectedCountry()
}
