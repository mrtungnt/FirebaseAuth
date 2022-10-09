package com.example.firebaseauth.auth

import android.location.Location
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
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
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val countryNamesAndDialCodesRepository: CountryNamesAndDialCodesRepository,
    private val savedSelectedCountryRepository: SavedSelectedCountryRepository,
    val authState: AuthUIState,
    private val savedState: SavedStateHandle
) :
    ViewModel(), PhoneAuthNotification {
    val snackbarHostState = SnackbarHostState()

    var shouldOpenLocationRequestPermissionRationaleDialog by mutableStateOf(false)

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
        val authState =
            authState.copy(
                authHomeUIState = authState.authHomeUIState.copy(
                    userSignedIn = Firebase.auth.currentUser != null, shouldShowLandingScreen =
                    authStateFlow.value.authHomeUIState.shouldShowLandingScreen
                )
            )
        savedState[stateKeyName] = authState
    }

    fun onEmptyDialCode() {
        val authRequestUIState = authStateFlow.value.authRequestUIState.copy(
            requestExceptionMessage = "Chưa chọn quốc gia"
        )
        savedState[stateKeyName] = authStateFlow.value.copy(
            authRequestUIState = authRequestUIState
        )
    }

    override fun onSuccessfulLogin() {
        val authHomeUIState = authStateFlow.value.authHomeUIState.copy(
            userSignedIn = Firebase.auth.currentUser != null
        )
        savedState[stateKeyName] = authStateFlow.value.copy(
            authHomeUIState = authHomeUIState
        )
    }

    override fun onVerificationException(exceptionMessage: String) {
        val authVerificationUIState = authStateFlow.value.authVerificationUIState.copy(
            verificationExceptionMessage = exceptionMessage
        )
        savedState[stateKeyName] = authStateFlow.value.copy(
            authVerificationUIState = authVerificationUIState
        )
    }

    override fun onCodeSent(
        verificationId: String,
        resendingToken: PhoneAuthProvider.ForceResendingToken
    ) {
        val authHomeUIState =
            authStateFlow.value.authHomeUIState.copy(
                verificationId = verificationId,
                resendingToken = resendingToken
            )
        savedState[stateKeyName] = authStateFlow.value.copy(
            authHomeUIState = authHomeUIState
        )
    }

    override fun onRequestException(exceptionMessage: String) {
        val authRequestUIState = authStateFlow.value.authRequestUIState.copy(
            requestExceptionMessage = exceptionMessage
        )
        savedState[stateKeyName] = authStateFlow.value.copy(
            authRequestUIState = authRequestUIState
        )
    }

    override fun onVerificationInProgress(inProgress: Boolean) {
        val authVerificationUIState = authStateFlow.value.authVerificationUIState.copy(
            verificationInProgress = inProgress

        )
        savedState[stateKeyName] = authStateFlow.value.copy(
            authVerificationUIState = authVerificationUIState
        )
    }

    override fun onRequestInProgress(inProgress: Boolean) {
        val authRequestUIState = authStateFlow.value.authRequestUIState.copy(
            requestInProgress = inProgress
        )
        savedState[stateKeyName] = authStateFlow.value.copy(
            authRequestUIState = authRequestUIState
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
                Timber.e("${exc.message}")
                updateSnackbar("Không khớp được tên quốc gia")
            }
        }
    }

    fun setShouldShowLandingScreen(decision: Boolean) {
        val authHomeUIState =
            authStateFlow.value.authHomeUIState.copy(shouldShowLandingScreen = decision)
        savedState[stateKeyName] = authStateFlow.value.copy(authHomeUIState = authHomeUIState)
    }

    fun dismissSnackbar() {
        snackbarHostState.currentSnackbarData?.dismiss()
    }

    fun onRequestTimeout() {
        val authRequestUIState =
            authStateFlow.value.authRequestUIState.copy(isRequestTimeout = true)
        savedState[stateKeyName] = authStateFlow.value.copy(authRequestUIState = authRequestUIState)
    }

    fun onVerificationTimeout() {
        val authVerificationUIState =
            authStateFlow.value.authVerificationUIState.copy(isVerificationTimeout = true)
        savedState[stateKeyName] =
            authStateFlow.value.copy(authVerificationUIState = authVerificationUIState)
    }

    fun updateSnackbar(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        dismissSnackbar()
        viewModelScope.launch {
            snackbarHostState.showSnackbar(message, duration = duration)
        }
    }

    fun openLocationRequestPermissionRationaleDialog() {
        shouldOpenLocationRequestPermissionRationaleDialog = true
    }

    var locationTask: Task<Location>? = null
    private var locationCancellationTokenSource = CancellationTokenSource()
    var locationCancellationToken = locationCancellationTokenSource.token
    fun cancelPendingActiveListener() {
        Timber.d(
            "locationTask -isSuccessful: ${locationTask?.isSuccessful ?: "null"} " +
                    "-isComplete: ${locationTask?.isComplete ?: "null"}"
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