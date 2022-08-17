package com.example.firebaseauth

import android.os.Parcelable
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

data class AuthState(
    var verificationId: String = "",
    var resendingToken: PhoneAuthProvider.ForceResendingToken = PhoneAuthProvider.ForceResendingToken.zza(),
    var user: FirebaseUser? = Firebase.auth.currentUser
)