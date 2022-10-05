package com.example.firebaseauth.auth

import android.os.Parcelable
import androidx.compose.material.SnackbarDuration
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
data class AuthUIState @Inject constructor(
    var authHomeUIState: AuthHomeUIState,
    var snackbarUIState: SnackbarUIState,
    var authRequestUIState: AuthRequestUIState,
    var authVerificationUIState: AuthVerificationUIState,
) : Parcelable {
    @Parcelize
    data class AuthHomeUIState @Inject constructor(
        var verificationId: String,
        var resendingToken: PhoneAuthProvider.ForceResendingToken,
        var userSignedIn: Boolean,
    ) : Parcelable

    @Parcelize
    data class SnackbarUIState @Inject constructor(
        var message: String,
        var duration: SnackbarDuration,
        var isSnackbarDisplayingWhileRequestingAuthCode: Boolean,
        var isSnackbarDisplayingWhileVerifyingAuthCode: Boolean,
    ) : Parcelable

    @Parcelize
    data class AuthRequestUIState @Inject constructor(
        var requestExceptionMessage: String,
        var requestInProgress: Boolean,
    ) : Parcelable

    @Parcelize
    data class AuthVerificationUIState @Inject constructor(
        var verificationExceptionMessage: String,
        var verificationInProgress: Boolean,
    ) : Parcelable
}