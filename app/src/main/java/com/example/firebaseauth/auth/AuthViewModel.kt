package com.example.firebaseauth.auth

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseauth.CountryNamesAndDialCodes
import com.example.firebaseauth.data.CountryNamesAndDialCodesRepository
import com.example.firebaseauth.data.SavedSelectedCountryRepository
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val countryNamesAndDialCodesRepository: CountryNamesAndDialCodesRepository,
    private val savedSelectedCountryRepository: SavedSelectedCountryRepository,
    private val authState: AuthUIState,
    private val savedState: SavedStateHandle
) :
    ViewModel(), PhoneAuthNotification {
    private val stateKeyName = "savedUIState"
    val authStateFlow = savedState.getStateFlow(stateKeyName, authState)

    private var _countriesAndDialCodes: List<CountryNamesAndDialCodes.NameAndDialCode> by mutableStateOf(
        emptyList()
    )
    val countriesAndDialCodes get() = _countriesAndDialCodes

    val flowOfSavedSelectedCountry get() = savedSelectedCountryRepository.getFlowOfSelectedCountry()

    private var _connectionExceptionMessage: String by mutableStateOf("")
    val connectionExceptionMessage get() = _connectionExceptionMessage

    init {
        viewModelScope.launch {
            val r = countryNamesAndDialCodesRepository.getCountriesAndDialCodes()
            if (r.isSuccess) {
                _countriesAndDialCodes = r.getOrNull()?.entriesList!!
            } else r.onFailure {
                _connectionExceptionMessage = it.message!!
            }
        }
    }

    fun logUserOut() {
        Firebase.auth.signOut()
        savedState[stateKeyName] = authState.copy(
            userSignedIn = Firebase.auth.currentUser != null
        )
    }

    fun onEmptyDialCode() {
        savedState[stateKeyName] = authStateFlow.value.copy(
            requestExceptionMessage = "Chưa chọn quốc gia"
        )
    }

    override fun onSuccessfulLogin() {
        savedState[stateKeyName] = authStateFlow.value.copy(
            userSignedIn = Firebase.auth.currentUser != null
        )
    }

    override fun onVerificationException(exceptionMessage: String) {
        savedState[stateKeyName] = authStateFlow.value.copy(
            verificationExceptionMessage = exceptionMessage
        )
    }

    override fun onCodeSent(
        verificationId: String,
        resendingToken: PhoneAuthProvider.ForceResendingToken
    ) {
        savedState[stateKeyName] = authStateFlow.value.copy(
            verificationId = verificationId,
            resendingToken = resendingToken
        )
    }

    override fun onRequestException(exceptionMessage: String) {
        savedState[stateKeyName] = authStateFlow.value.copy(
            requestExceptionMessage = exceptionMessage
        )
    }

    override fun onVerificationInProgress(inProgress: Boolean) {
        savedState[stateKeyName] = authStateFlow.value.copy(
            verificationInProgress = inProgress
        )
    }

    override fun onRequestInProgress(inProgress: Boolean) {
        savedState[stateKeyName] = authStateFlow.value.copy(
            requestInProgress = inProgress
        )
    }

    fun saveSelectedCountry(selectedCountry: CountryNamesAndDialCodes.NameAndDialCode) {
        viewModelScope.launch {
            savedSelectedCountryRepository.saveSelectedCountry(selectedCountry)
        }
    }

    fun setSelectedCountry(countryName: String) {
        viewModelScope.launch {
            try {
                saveSelectedCountry(_countriesAndDialCodes.first { it.name == countryName })
            } catch (exc: NoSuchElementException) {
                Log.e("NoSuchElementException", "msg: ${exc.message}")
                updateSnackbar("Không khớp được tên quốc gia")
            }
        }
    }

    fun clearSnackbar() {
        if (authStateFlow.value.snackbarMsg.isNotEmpty())
            savedState[stateKeyName] = authStateFlow.value.copy(snackbarMsg = "")
    }

    fun updateSnackbar(message: String, infinite: Boolean = false) {
        if (!infinite && message.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.Default) {
                delay(4000L)
                savedState[stateKeyName] = authStateFlow.value.copy(snackbarMsg = "")
            }
        }
        savedState[stateKeyName] = authStateFlow.value.copy(snackbarMsg = message)
    }

    var locationTask: Task<Location>? = null
    private var locationCancellationTokenSource = CancellationTokenSource()
    var locationCancellationToken = locationCancellationTokenSource.token
    fun cancelPendingActiveListener() {
        Log.d(
            "locationTask",
            "in cancelPendingActiveListener() -isSuccessful: ${locationTask?.isSuccessful ?: "null"} " +
                    "-isComplete ${locationTask?.isComplete ?: "null"}"
        )
        if (locationTask?.isSuccessful == false) {
            if (!locationCancellationToken.isCancellationRequested) {
                locationCancellationTokenSource.cancel()

                locationCancellationTokenSource = CancellationTokenSource()
                locationCancellationToken = locationCancellationTokenSource.token
            }
        }
    }
}