package com.example.firebaseauth.auth

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import javax.inject.Inject

data class AuthState @Inject constructor(
    var verificationId: String,
    var resendingToken: PhoneAuthProvider.ForceResendingToken,
    var user: FirebaseUser?
)