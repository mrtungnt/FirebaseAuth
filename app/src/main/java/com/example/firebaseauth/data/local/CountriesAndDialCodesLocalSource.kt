package com.example.firebaseauth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import com.example.firebaseauth.CountriesAndDialCodes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

 class CountriesAndDialCodesLocalSource @Inject constructor(val dataStore: DataStore<CountriesAndDialCodes>) {
//    val dataStore:DataStore<CountriesAndDialCodes> = context.createDa
    fun getData() = dataStore.data
    suspend fun getCountriesAndDialCodes() = dataStore.data.first()
}