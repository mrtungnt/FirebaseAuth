/*
package com.example.firebaseauth.data

import com.example.firebaseauth.data.local.SelectedCountryService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface SavedSelectedCountryRepository {
    suspend fun saveSelectedCountry(selectedCountry: SelectedCountry)

    fun getFlowOfSelectedCountry(): Flow<SelectedCountry>
}

class SavedSelectedCountryRepositoryImpl @Inject constructor(private val source: SelectedCountryService) :
    SavedSelectedCountryRepository {
    override suspend fun saveSelectedCountry(selectedCountry: SelectedCountry) {
        source.saveSelectedCountry(selectedCountry)
    }

    override fun getFlowOfSelectedCountry() = source.getFlowOfSelectedCountry()
}*/
