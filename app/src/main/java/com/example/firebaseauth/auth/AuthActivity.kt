package com.example.firebaseauth.auth

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.firebaseauth.ui.HomeContent
import com.example.firebaseauth.ui.NoConnectionScreen
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    val authViewModel by viewModels<AuthViewModel>()

    @RequiresApi(Build.VERSION_CODES.N)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(
                Manifest.permission.ACCESS_FINE_LOCATION, false
            ) -> {
                // Precise location access granted.
                whenLocationPermissionGranted()
            }

            permissions.getOrDefault(
                Manifest.permission.ACCESS_COARSE_LOCATION, false
            ) -> {
                // Only approximate location access granted.
                whenLocationPermissionGranted()
            }

            else -> {
                // No location access granted.
                authViewModel.updateSnackbar("Chưa được cấp quyền dùng Định vị.")
            }
        }
    }

    private val intentSenderForEnablingLocation =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult())
        { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                whenLocationReady()
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    fun handleLocationPermissionRequest() {
        val permissionCheck = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        when {
            permissionCheck == PackageManager.PERMISSION_GRANTED -> {
                whenLocationPermissionGranted()
            }

            shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
authViewModel.updateSnackbar(
                    "Hãy cho phép dùng Định vị (Location) để sử dụng tính năng này.",
                    duration = SnackbarDuration.Short
                )

                authViewModel.openLocationRequestPermissionRationaleDialog()
            }

            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private suspend fun getFromLocation(location: Location): List<Address>? {
        return withContext(context = lifecycleScope.coroutineContext + Dispatchers.IO) {
            try {
                Geocoder(applicationContext).getFromLocation(
                    location.latitude, location.longitude, 1
                )
            } catch (exc: java.lang.Exception) {
                Timber.e("${exc.message}")
                emptyList()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun whenLocationReady() {
        authViewModel.updateSnackbar(
            "Đang xác định quốc gia từ vị trí. Trong một số điều kiện, có thể mất 30 giây.",
            SnackbarDuration.Indefinite
        )
        authViewModel.locationTask = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, authViewModel.locationCancellationToken
        )
        authViewModel.locationTask?.addOnSuccessListener {
            if (it != null) {
                lifecycleScope.launch {
                    val address = getFromLocation(it)
                    if (address?.isNotEmpty() == true) {
//                        authViewModel.setSelectedCountry(address.first().countryName)
                        authViewModel.updateSnackbar("Đã xác định quốc gia từ vị trí.")
                    }
                }
            } else {
                authViewModel.updateSnackbar("Không xác định được vị trí.")
            }
        }
        authViewModel.locationTask?.addOnFailureListener {
            authViewModel.updateSnackbar(message = it.message!!)
        }
    }

    private fun whenLocationPermissionGranted() {
        val locationSettingsRequestBuilder =
            LocationSettingsRequest.Builder().addLocationRequest(LocationRequest.create())
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)
        val taskLocationSettingsResponse: Task<LocationSettingsResponse> =
            settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build())

        taskLocationSettingsResponse.addOnSuccessListener {
            whenLocationReady()
        }
        taskLocationSettingsResponse.addOnFailureListener {
            if (it is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling intentSenderForEnablingLocation.launch((),
                    // and check the result in the callback of registerForActivityResult().
                    val status = it.status
                    if (status.hasResolution()) {
                        val pendingIntent: PendingIntent = status.resolution!!
//                        Preconditions.checkNotNull(pendingIntent)
                        intentSenderForEnablingLocation.launch(
                            IntentSenderRequest.Builder(pendingIntent.intentSender)
.setFillInIntent(null)
                                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION, 0)
.build()
                        )
                    }
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    Timber.e(
                        "${it.message}"
                    )
                }
            }
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var isConnected by mutableStateOf(false)

    private val callbacks = object : CallbacksFromPhoneAuthToHost() {
        override fun onCodeSent(
            verificationId: String, resendingToken: PhoneAuthProvider.ForceResendingToken
        ) {
            authViewModel.onCodeSent(verificationId, resendingToken)
        }

        override fun onSuccessfulLogin() {
            authViewModel.onSuccessfulLogin()
        }

        override fun onRequestException(exceptionMessage: String) {
            authViewModel.onRequestException(exceptionMessage)
        }

        override fun onVerificationException(exceptionMessage: String) {
            authViewModel.onVerificationException(exceptionMessage)
        }

        override fun onVerificationInProgress(inProgress: Boolean) {
            authViewModel.onVerificationInProgress(inProgress)
        }

        override fun onRequestInProgress(inProgress: Boolean) {
            authViewModel.onRequestInProgress(inProgress)
        }
    }

    val phoneAuth = PhoneAuth(
        activity = this@AuthActivity,
        callbacks,
    )

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.registerNetworkCallback(
            NetworkRequest.Builder().build(), NetworkCallbackExt(this)
        )

//        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            if (isConnected) HomeContent()
            else NoConnectionScreen()
        }
    }
}

class NetworkCallbackExt(private val activity: AuthActivity) :
    ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        activity.isConnected = true
    }

    override fun onUnavailable() {
        super.onUnavailable()
        activity.isConnected = false
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        activity.isConnected = false
    }
}
