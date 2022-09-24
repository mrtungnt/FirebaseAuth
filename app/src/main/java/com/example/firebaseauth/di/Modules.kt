package com.example.firebaseauth.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.example.firebaseauth.CountryNamesAndDialCodes
import com.example.firebaseauth.SelectedCountry
import com.example.firebaseauth.auth.AuthUIState
import com.example.firebaseauth.data.CountryNamesAndDialCodesSerializer
import com.example.firebaseauth.data.SelectedCountrySerializer
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class CountryNamesAndDialCodesDataStore

@Qualifier
annotation class SelectedCountryDataStore

@Module
@InstallIn(SingletonComponent::class)
object Modules {
    @Singleton
    @Provides
    fun providesAuthUIState(): AuthUIState = AuthUIState(
        "", PhoneAuthProvider.ForceResendingToken.zza(),
        Firebase.auth.currentUser != null,
        null,
        null,
        requestInProgress = false,
        verificationInProgress = false,
    )

    @CountryNamesAndDialCodesDataStore
    @Singleton
    @Provides
    fun providesCountryNamesAndDialCodesDataStore(@ApplicationContext context: Context): DataStore<CountryNamesAndDialCodes> =
        DataStoreFactory.create(serializer = CountryNamesAndDialCodesSerializer, produceFile = {
            context.dataStoreFile(
                COUNTRY_NAMES_AND_DIAL_CODES_FILE_NAME
            )
        })

    @SelectedCountryDataStore
    @Singleton
    @Provides
    fun providesSelectedCountryDataStore(@ApplicationContext context: Context): DataStore<SelectedCountry> =
        DataStoreFactory.create(serializer = SelectedCountrySerializer, produceFile = {
            context.dataStoreFile(
                SAVED_SELECTED_COUNTRY_FILE_NAME
            )
        })
}

const val COUNTRY_NAMES_AND_DIAL_CODES_FILE_NAME = "country_names_and_dial_codes.pb"
const val SAVED_SELECTED_COUNTRY_FILE_NAME = "saved_selected_country.pb"