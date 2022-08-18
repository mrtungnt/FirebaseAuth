package com.example.firebaseauth.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(authState: AuthState, private val state: SavedStateHandle) :
    ViewModel() {
    private val _authStateFlow = MutableStateFlow(authState)
    val authStateFlow: StateFlow<AuthState>
        get() = _authStateFlow

    fun onCodeSent(
        verificationId: String,
        resendingToken: PhoneAuthProvider.ForceResendingToken
    ) {
        _authStateFlow.update {
            it.copy(
                verificationId = verificationId,
                resendingToken = resendingToken
            )
        }
    }

    fun onSuccessfulLogin(user: FirebaseUser?) {
        _authStateFlow.update { it.copy(user = user) }
    }

    fun logUserOut() {
        Firebase.auth.signOut()
        _authStateFlow.update { it.copy(user = Firebase.auth.currentUser, verificationId = "") }
    }
}