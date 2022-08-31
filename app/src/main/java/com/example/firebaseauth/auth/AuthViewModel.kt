package com.example.firebaseauth.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseauth.data.CountryAndDialCodeModel
import com.example.firebaseauth.data.CountryAndDialCodeRepository
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val countryAndDialCodeRepository: CountryAndDialCodeRepository,
    private val authState: AuthUIState,
    private val state: SavedStateHandle
) :
    ViewModel(), PhoneAuthNotification {
    private val stateKeyName = "savedUIState"
    val authStateFlow = state.getStateFlow(stateKeyName, authState)

    private lateinit var _countriesAndDialCodes: List<CountryAndDialCodeModel>
    val countriesAndDialCodes
        get() = _countriesAndDialCodes
    private var _countriesAndDialCodesReady by mutableStateOf(false)
    val countriesAndDialCodesReady get() = _countriesAndDialCodesReady

    private var _connectionExceptionMessage: String? by mutableStateOf(null)
    val connectionExceptionMessage get() = _connectionExceptionMessage

    init {
        viewModelScope.launch {
            val r = countryAndDialCodeRepository.getCountriesAndDialCodes()
            if (r.isSuccess) {
                _countriesAndDialCodes = r.getOrNull()?.data!!
                _countriesAndDialCodesReady = true
            } else r.onFailure { _connectionExceptionMessage = it.message }
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