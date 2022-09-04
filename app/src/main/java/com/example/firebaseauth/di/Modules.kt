package com.example.firebaseauth.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.example.firebaseauth.CountriesAndDialCodes
import com.example.firebaseauth.auth.AuthUIState
import com.example.firebaseauth.data.CountriesAndDialCodesSerializer
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
}

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {
    @Singleton
    @Provides
    fun providesDataStore(@ApplicationContext context: Context): DataStore<CountriesAndDialCodes> =
        DataStoreFactory.create(serializer = CountriesAndDialCodesSerializer, produceFile = {
            context.dataStoreFile(
                DATA_STORE_FILE_NAME
            )
        })
}

const val DATA_STORE_FILE_NAME = "datastore.pb"