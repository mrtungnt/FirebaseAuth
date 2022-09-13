package com.example.firebaseauth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import com.example.firebaseauth.CountriesAndDialCodes
import com.example.firebaseauth.di.CountriesAndDialCodesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

 class CountriesAndDialCodesLocalSource @Inject constructor(@CountriesAndDialCodesDataStore val dataStore: DataStore<CountriesAndDialCodes>) {
    fun getData() = dataStore.data
}