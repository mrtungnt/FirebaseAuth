package com.example.firebaseauth.auth

import android.os.Parcelable
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
data class AuthUIState @Inject constructor(
    var verificationId: String,
    var resendingToken: PhoneAuthProvider.ForceResendingToken,
    var user: FirebaseUser?,
    var phoneNumberException: FirebaseAuthInvalidCredentialsException?,
    var verificationCodeException: FirebaseAuthInvalidCredentialsException?,
    var waitingForVerificationCode: Boolean,
    var waitingForLoggingCompletion: Boolean,
) : Parcelable