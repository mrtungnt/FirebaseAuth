package com.example.firebaseauth.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authState: AuthUIState,
    private val state: SavedStateHandle
) :
    ViewModel() {
    private val stateKeyName = "savedUIState"

    //    private val _authStateFlow = MutableStateFlow(authState)
//    val authStateFlow = _authStateFlow.asStateFlow()
    val authStateFlow = state.getStateFlow(stateKeyName, authState)

    fun onCodeSent(
        verificationId: String,
        resendingToken: PhoneAuthProvider.ForceResendingToken
    ) {
        /*_authStateFlow.update {
            it.copy(
                verificationId = verificationId,
                resendingToken = resendingToken
            )
        }*/
        state[stateKeyName] = authStateFlow.value.copy(
            verificationId = verificationId,
            resendingToken = resendingToken
        )
    }

    fun onSuccessfulLogin(user: FirebaseUser?) {
//        _authStateFlow.update { it.copy(user = user) }
        state[stateKeyName] = authStateFlow.value.copy(user = user)
    }

    fun logUserOut() {
        Firebase.auth.signOut()
//        _authStateFlow.update { authState.copy(user = Firebase.auth.currentUser) }
        state[stateKeyName] = authStateFlow.value.copy(user = Firebase.auth.currentUser)
    }

    fun onPhoneNumberException(exception: FirebaseAuthInvalidCredentialsException) {
//        _authStateFlow.update { it.copy(phoneNumberException = exception) }
        state[stateKeyName] = authStateFlow.value.copy(
            phoneNumberException = exception
        )
    }

    fun onVerificationCodeException(exception: FirebaseAuthInvalidCredentialsException) {
//        _authStateFlow.update { it.copy(verificationCodeException = exception) }
        state[stateKeyName] = authStateFlow.value.copy(
            verificationCodeException = exception
        )
    }

    fun clearVerificationCodeException() {
//        _authStateFlow.update { it.copy(verificationCodeException = null) }
        state[stateKeyName] = authStateFlow.value.copy(
            verificationCodeException = null
        )
    }

    fun onVerificationProgressNotification(progress: Boolean) {
//        _authStateFlow.update { it.copy(waitingForVerificationCode = progress) }
        state[stateKeyName] = authStateFlow.value.copy(
            waitingForVerificationCode = progress
        )
    }
}