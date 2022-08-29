package com.example.firebaseauth.auth

import android.util.Log
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

interface PhoneAuthNotification {
    fun onSuccessfulLogin()

    fun onVerificationException(exceptionMessage: String)

    fun onCodeSent(
        verificationId: String,
        resendingToken: PhoneAuthProvider.ForceResendingToken
    )

    fun onRequestException(exceptionMessage: String)

    fun onVerificationInProgress(inProgress: Boolean)

    fun onRequestInProgress(inProgress: Boolean)
}

abstract class CallbacksFromPhoneAuthToHost : PhoneAuthNotification {
    abstract override fun onSuccessfulLogin()

    abstract override fun onVerificationException(exceptionMessage: String)

    override fun onCodeSent(
        verificationId: String,
        resendingToken: PhoneAuthProvider.ForceResendingToken
    ) {
    }

    override fun onRequestException(exceptionMessage: String) {}

    override fun onVerificationInProgress(inProgress: Boolean) {}

    override fun onRequestInProgress(inProgress: Boolean) {}
}

class PhoneAuth(
    private val activity: ComponentActivity,
    private val callbacksToHost: CallbacksFromPhoneAuthToHost,
) {
    companion object {
        private const val TAG = "PhoneAuthActivity"
    }

    private val auth: FirebaseAuth = Firebase.auth

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)
//                if (e) is FirebaseTooManyRequestsException -> {} // The SMS quota for the project has been exceeded
            callbacksToHost.onRequestException(e.message!!)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save resending token so we can use it later
            resendToken = token
            callbacksToHost.onCodeSent(verificationId, token)
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            Log.d(TAG, "onCodeAutoRetrievalTimeOut:$verificationId")
        }
    }

    fun startPhoneNumberVerification(
        phoneNumber: String,
        resendToken: PhoneAuthProvider.ForceResendingToken
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setForceResendingToken(resendToken)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          //
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
        callbacksToHost.onRequestInProgress(false)
        callbacksToHost.onRequestInProgress(true)
    }

    fun onReceiveCodeToVerify(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
        callbacksToHost.onVerificationInProgress(true)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    callbacksToHost.onVerificationInProgress(false)
                    callbacksToHost.onSuccessfulLogin()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        callbacksToHost.onVerificationException((task.exception as FirebaseAuthInvalidCredentialsException).message!!)
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }
}