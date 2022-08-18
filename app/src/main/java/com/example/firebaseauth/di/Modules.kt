package com.example.firebaseauth.di

import com.example.firebaseauth.auth.AuthState
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object Modules {
    @Singleton
    @Provides
    fun providesAuthState(): AuthState = AuthState(
        "", PhoneAuthProvider.ForceResendingToken.zza(),
        Firebase.auth.currentUser
    )
}