package com.example.firebaseauth.data.local

import androidx.datastore.core.DataStore
import com.example.firebaseauth.SelectedCountry
import com.example.firebaseauth.di.CountriesAndDialCodesDataStore
import com.example.firebaseauth.di.SelectedCountryDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SelectedCountryLocalSource @Inject constructor(@SelectedCountryDataStore val dataStore: DataStore<SelectedCountry>) {
    suspend fun getSelectedCountry() = Result.success(dataStore.data.first().selectedCountry)
    fun getFlowOfSelectedCountry() = dataStore.data
}