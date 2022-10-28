package com.example.firebaseauth.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.example.firebaseauth.auth.AuthUIState
import com.example.firebaseauth.data.*
import com.example.firebaseauth.data.CountryNamesAndCallingCodesService
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class CountryNamesAndCallingCodesDataStore

@Qualifier
annotation class SelectedCountryDataStore

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceBindersModule {
    @Singleton
    @Binds
    abstract fun bindsCountryNamesAndDialCodesRepository(implementation: CountryNamesAndDialCodesRepositoryImpl): CountryNamesAndDialCodesRepository

    @Singleton
    @Binds
    abstract fun bindsSavedSelectedCountryRepository(impl: SavedSelectedCountryRepositoryImpl): SavedSelectedCountryRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AuthHomeUIStateModule {
    @Singleton
    @Provides
    fun providesAuthHomeUIState(): AuthUIState.AuthHomeUIState = AuthUIState.AuthHomeUIState(
        "", PhoneAuthProvider.ForceResendingToken.zza(),
        Firebase.auth.currentUser != null,
        true,
    )
}

@Module
@InstallIn(SingletonComponent::class)
object ClassProvidersModule {
    @Singleton
    @Provides
    fun providesAuthRequestUIState(): AuthUIState.AuthRequestUIState =
        AuthUIState.AuthRequestUIState(
            "",
            requestInProgress = false,
            false
        )

    @Singleton
    @Provides
    fun providesAuthVerificationUIState(): AuthUIState.AuthVerificationUIState =
        AuthUIState.AuthVerificationUIState(
            "",
            false,
            isVerificationTimeout = false
        )

    @Singleton
    @Provides
    fun providesCountryNamesAndDialCodesLocalSource(@ApplicationContext context: Context) =
        CountryNamesAndCallingCodesService(context)

    @CountryNamesAndCallingCodesDataStore
    @Singleton
    @Provides
    fun providesCountryNamesAndDialCodesDataStore(@ApplicationContext context: Context): DataStore<CountryNamesAndDialCodes> =
        DataStoreFactory.create(serializer = CountryNamesAndDialCodesSerializer) {
            context.dataStoreFile(
                COUNTRY_NAMES_AND_DIAL_CODES_FILE_NAME
            )
        }

    @SelectedCountryDataStore
    @Singleton
    @Provides
    fun providesSelectedCountryDataStore(@ApplicationContext context: Context): DataStore<SelectedCountry> =
        DataStoreFactory.create(serializer = SelectedCountrySerializer) {
            context.dataStoreFile(
                SAVED_SELECTED_COUNTRY_FILE_NAME
            )
        }
}

const val COUNTRY_NAMES_AND_DIAL_CODES_FILE_NAME = "country_names_and_dial_codes.pb"
const val SAVED_SELECTED_COUNTRY_FILE_NAME = "saved_selected_country.pb"
