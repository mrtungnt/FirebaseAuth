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
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.firebaseauth.repositories.CountryNamesAndCallingCodesRepository
import com.example.firebaseauth.repositories.SavedSelectedCountryRepository
import com.example.firebaseauth.services.CountryNamesAndCallingCodeModel
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val savedSelectedCountryRepository: SavedSelectedCountryRepository,
    private val countryNamesAndCallingCodesRepository: CountryNamesAndCallingCodesRepository,
    val authUIState: AuthUIState,
    private val savedState: SavedStateHandle
) :
    ViewModel(), PhoneAuthNotification {
    val snackbarHostState = SnackbarHostState()

    var shouldOpenLocationRequestPermissionRationaleDialog by mutableStateOf(false)

    private val stateKeyName = "savedUIState"

    val authUIStateFlow = savedState.getStateFlow(stateKeyName, authUIState)

    private var _countriesAndCallingCodes: List<CountryNamesAndCallingCodeModel> by mutableStateOf(
        emptyList()
    )

    val countriesAndCallingCodes get() = _countriesAndCallingCodes

    var savedSelectedCountryState: CountryNamesAndCallingCodeModel? by mutableStateOf(null)

    val countryNamesAndCallingCodesPager = Pager<Int, CountryNamesAndCallingCodeModel>(
        PagingConfig(
            pageSize = countryNamesAndCallingCodesRepository.pageSize,
//            enablePlaceholders = true
        )
    ) { countryNamesAndCallingCodesRepository }

    var countryNamesAndCallingCodesSearchResult by mutableStateOf(emptyList<CountryNamesAndCallingCodeModel>())

    init {
        viewModelScope.launch {
            try {
                _countriesAndCallingCodes =
                    countryNamesAndCallingCodesRepository.getCountryNamesAndCallingCodes()
            } catch (exc: Exception) {
                snackbarHostState.showSnackbar(
                    exc.message
                        ?: "${exc.toString()} while getting country names and calling codes."
                )
            }
        }

        viewModelScope.launch {
            try {
                savedSelectedCountryState = savedSelectedCountryRepository.getSelectedCountry()
            } catch (exc: Exception) {
                snackbarHostState.showSnackbar(
                    exc.message ?: "${exc.toString()} while getting selected country."
                )
            }
        }
    }


    fun logUserOut() {
        Firebase.auth.signOut()
        val authState =
            authUIState.copy(
                authHomeUIState = authUIState.authHomeUIState.copy(
                    userSignedIn = Firebase.auth.currentUser != null, shouldShowLandingScreen =
                    authUIStateFlow.value.authHomeUIState.shouldShowLandingScreen
                )
            )
        savedState[stateKeyName] = authState
    }

    override fun onSuccessfulLogin() {
        val authHomeUIState = authUIStateFlow.value.authHomeUIState.copy(
            userSignedIn = Firebase.auth.currentUser != null
        )
        savedState[stateKeyName] = authUIStateFlow.value.copy(
            authHomeUIState = authHomeUIState
        )
    }

    override fun onVerificationException(exceptionMessage: String) {
        val authVerificationUIState = authUIStateFlow.value.authVerificationUIState.copy(
            verificationExceptionMessage = exceptionMessage
        )
        savedState[stateKeyName] = authUIStateFlow.value.copy(
            authVerificationUIState = authVerificationUIState
        )
    }

    override fun onCodeSent(
        verificationId: String,
        resendingToken: PhoneAuthProvider.ForceResendingToken
    ) {
        val authHomeUIState =
            authUIStateFlow.value.authHomeUIState.copy(
                verificationId = verificationId,
                resendingToken = resendingToken
            )
        savedState[stateKeyName] = authUIStateFlow.value.copy(
            authHomeUIState = authHomeUIState
        )
    }

    override fun onRequestException(exceptionMessage: String) {
        val authRequestUIState = authUIStateFlow.value.authRequestUIState.copy(
            requestExceptionMessage = exceptionMessage
        )
        savedState[stateKeyName] = authUIStateFlow.value.copy(
            authRequestUIState = authRequestUIState
        )
    }

    override fun onVerificationInProgress(inProgress: Boolean) {
        val authVerificationUIState = authUIStateFlow.value.authVerificationUIState.copy(
            verificationInProgress = inProgress

        )
        savedState[stateKeyName] = authUIStateFlow.value.copy(
            authVerificationUIState = authVerificationUIState
        )
    }

    override fun onRequestInProgress(inProgress: Boolean) {
        val authRequestUIState = authUIStateFlow.value.authRequestUIState.copy(
            requestInProgress = inProgress
        )
        savedState[stateKeyName] = authUIStateFlow.value.copy(
            authRequestUIState = authRequestUIState
        )
    }

    fun saveSelectedCountry(selectedCountry: CountryNamesAndCallingCodeModel) {
        viewModelScope.launch {
            savedSelectedCountryRepository.saveSelectedCountry(selectedCountry)
        }
    }

    fun setSelectedCountry(countryCode: String) {
        viewModelScope.launch {
            try {
                savedSelectedCountryState =
                    _countriesAndCallingCodes.first { it.name == countryCode }
                saveSelectedCountry(_countriesAndCallingCodes.first { it.name == countryCode })
            } catch (exc: NoSuchElementException) {
                Timber.e("${exc.message}")
                updateSnackbar("Không khớp được tên quốc gia")
            }
        }
    }


    fun setShouldShowLandingScreen(decision: Boolean) {
        val authHomeUIState =
            authUIStateFlow.value.authHomeUIState.copy(shouldShowLandingScreen = decision)
        savedState[stateKeyName] = authUIStateFlow.value.copy(authHomeUIState = authHomeUIState)
    }

    fun dismissSnackbar() {
        snackbarHostState.currentSnackbarData?.dismiss()
    }

    fun onRequestTimeout() {
        val authRequestUIState =
            authUIStateFlow.value.authRequestUIState.copy(isRequestTimeout = true)
        savedState[stateKeyName] =
            authUIStateFlow.value.copy(authRequestUIState = authRequestUIState)
    }

    fun onVerificationTimeout() {
        val authVerificationUIState =
            authUIStateFlow.value.authVerificationUIState.copy(isVerificationTimeout = true)
        savedState[stateKeyName] =
            authUIStateFlow.value.copy(authVerificationUIState = authVerificationUIState)
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

    private var lastSearchJob: Job? = null

    fun searchCountryNamesAndCallingCodes(keyword: String) {
        viewModelScope.launch {
            if (lastSearchJob?.isActive == true) {
                lastSearchJob?.cancel()
                Timber.d("lastSearchJob was cancelled.")
            }
            lastSearchJob = this.coroutineContext.job
            delay(450)
            countryNamesAndCallingCodesSearchResult =
                countryNamesAndCallingCodesRepository.searchCountryNamesAndCallingCodes(keyword)
        }
    }

    fun clearCountryNamesAndCallingCodesSearchResult() {
        countryNamesAndCallingCodesSearchResult = emptyList()
    }
}
