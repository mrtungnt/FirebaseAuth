package com.example.firebaseauth.data

import com.example.firebaseauth.data.local.SelectedCountryLocalSource
import javax.inject.Inject

class SelectedCountryRepository @Inject constructor(private val localSource: SelectedCountryLocalSource) {
    suspend fun getSelectedCountry() = localSource.getSelectedCountry()
}