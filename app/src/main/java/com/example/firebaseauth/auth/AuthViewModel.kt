package com.example.firebaseauth.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseauth.data.CountryModel
import com.example.firebaseauth.data.CountryRepository
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val countryRepository: CountryRepository,
    private val authState: AuthUIState,
    private val state: SavedStateHandle
) :
    ViewModel(), PhoneAuthNotification {
    private val stateKeyName = "savedUIState"
    val authStateFlow = state.getStateFlow(stateKeyName, authState)

    val countriesAndDialCodes: Map<String, String> = mapOf()

    init {
        viewModelScope.launch {
            val r = countryRepository.getCountriesAndDialCodes()
            if (r.isSuccess) {
                r.getOrNull()?.data?.forEach { countriesAndDialCodes[it.name] to it.dial_code }
            }
        }
    }

    fun logUserOut() {
        Firebase.auth.signOut()
        state[stateKeyName] =
            authState.copy(userSignedIn = Firebase.auth.currentUser != null)
    }

    fun clearVerificationExceptionMessage() {
        state[stateKeyName] = authStateFlow.value.copy(
            verificationExceptionMessage = null,
        )
    }

    fun clearRequestExceptionMessage() {
        state[stateKeyName] =
            authStateFlow.value.copy(
                requestExceptionMessage = null,
            )
    }

    override fun onSuccessfulLogin() {
        state[stateKeyName] =
            authStateFlow.value.copy(
                userSignedIn = Firebase.auth.currentUser != null
            )
    }

    override fun onVerificationException(exceptionMessage: String) {
        state[stateKeyName] = authStateFlow.value.copy(
            verificationExceptionMessage = exceptionMessage
        )
    }

    override fun onCodeSent(
        verificationId: String,
        resendingToken: PhoneAuthProvider.ForceResendingToken
    ) {
        state[stateKeyName] = authStateFlow.value.copy(
            verificationId = verificationId,
            resendingToken = resendingToken
        )
    }

    override fun onRequestException(exceptionMessage: String) {
        state[stateKeyName] = authStateFlow.value.copy(
            requestExceptionMessage = exceptionMessage
        )
    }

    override fun onVerificationInProgress(inProgress: Boolean) {
        state[stateKeyName] = authStateFlow.value.copy(
            verificationInProgress = inProgress
        )
    }

    override fun onRequestInProgress(inProgress: Boolean) {
        state[stateKeyName] = authStateFlow.value.copy(
            requestInProgress = inProgress
        )
    }
}