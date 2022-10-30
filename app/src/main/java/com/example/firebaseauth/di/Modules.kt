package com.example.firebaseauth.di

import android.content.Context
import com.example.firebaseauth.auth.AuthUIState
import com.example.firebaseauth.repositories.*
import com.example.firebaseauth.services.CountryNamesAndCallingCodesService
import com.example.firebaseauth.services.SavedSelectedCountryService
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InterfaceBindersModule {
    @Singleton
    @Binds
    abstract fun bindsCountryNamesAndCallingCodesRepository(implementation: CountryNamesAndCallingCodesRepositoryImpl): CountryNamesAndCallingCodesRepository

    @Singleton
    @Binds
    abstract fun bindsSavedSelectedCountryRepository(impl: SavedSelectedCountryRepositoryImpl): SavedSelectedCountryRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ServiceProvidersModule {
    @Singleton
    @Provides
    fun providesSavedSelectedCountryService(@ApplicationContext context: Context) =
        SavedSelectedCountryService(context)
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
}
