package com.example.firebaseauth.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authState: AuthUIState,
    private val state: SavedStateHandle
) :
    ViewModel() {
    private val _authStateFlow = MutableStateFlow(authState)
    val authStateFlow = _authStateFlow.asStateFlow()

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
        _authStateFlow.update { authState.copy(user = Firebase.auth.currentUser) }
    }

    fun onPhoneNumberException(exception: FirebaseAuthInvalidCredentialsException) {
        _authStateFlow.update { it.copy(phoneNumberException = exception) }
    }

    fun onVerificationCodeException(exception: FirebaseAuthInvalidCredentialsException) {
        _authStateFlow.update { it.copy(verificationCodeException = exception) }
    }

    fun clearVerificationCodeException() {
        _authStateFlow.update { it.copy(verificationCodeException = null) }
    }

    fun onVerificationProgressNotification(progress: Boolean) {
        _authStateFlow.update { it.copy(waitingForVerificationCode = progress) }
    }
}