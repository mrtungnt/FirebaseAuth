package com.example.firebaseauth.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
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
    ViewModel(), PhoneAuthNotification {
    private val stateKeyName = "savedUIState"
    val authStateFlow = state.getStateFlow(stateKeyName, authState)

    fun logUserOut() {
        Firebase.auth.signOut()
        state[stateKeyName] =
            authState.copy(user = Firebase.auth.currentUser)
    }

    fun clearVerificationCodeException() {
        state[stateKeyName] = authStateFlow.value.copy(
            verificationCodeException = null
        )
    }

    override fun notifySuccessfulLogin(user: FirebaseUser?) {
        state[stateKeyName] = authStateFlow.value.copy(user = user)
    }

    override fun notifyVerificationCodeException(exception: FirebaseAuthInvalidCredentialsException) {
        state[stateKeyName] = authStateFlow.value.copy(
            verificationCodeException = exception
        )
    }

    override fun notifyCodeSent(
        verificationId: String,
        resendingToken: PhoneAuthProvider.ForceResendingToken
    ) {
        state[stateKeyName] = authStateFlow.value.copy(
            verificationId = verificationId,
            resendingToken = resendingToken
        )
    }

    override fun notifyPhoneNumberException(exception: FirebaseException) {
        state[stateKeyName] = authStateFlow.value.copy(
            phoneNumberException = exception
        )
    }

    override fun notifyVerificationProgress(progress: Boolean) {
        state[stateKeyName] = authStateFlow.value.copy(
            waitingForVerificationCode = progress
        )
    }

    override fun notifyLoggingProgress(progress: Boolean) {
        TODO("Not yet implemented")
    }
}