package com.example.firebaseauth.data

import com.example.firebaseauth.CountryNamesAndDialCodes
import com.example.firebaseauth.SelectedCountry
import com.example.firebaseauth.data.local.SelectedCountryLocalSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface SavedSelectedCountryRepository{
    suspend fun saveSelectedCountry(selectedCountry: CountryNamesAndDialCodes.NameAndDialCode)

    fun getFlowOfSelectedCountry(): Flow<SelectedCountry>
}

class SavedSelectedCountryRepositoryImpl @Inject constructor(private val localSource: SelectedCountryLocalSource):SavedSelectedCountryRepository {
    override suspend fun saveSelectedCountry(selectedCountry: CountryNamesAndDialCodes.NameAndDialCode) {
        localSource.saveSelectedCountry(selectedCountry)
    }

    override fun getFlowOfSelectedCountry() = localSource.getFlowOfSelectedCountry()
}