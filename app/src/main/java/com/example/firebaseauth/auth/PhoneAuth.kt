package com.example.firebaseauth.auth

import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class CallbacksFromPhoneAuthToHost {
    open fun notifyCodeSent(
        verificationId: String,
        resendingToken: PhoneAuthProvider.ForceResendingToken
    ) {
    }

    abstract fun notifySuccessfulLogin(user: FirebaseUser?)

    open fun notifyPhoneNumberException(exception: FirebaseAuthInvalidCredentialsException) {}

    abstract fun notifyVerificationCodeException(exception: FirebaseAuthInvalidCredentialsException)

    open fun notifyVerificationProgress(progress: Boolean) {}

    open fun notifyLoggingProgress(progress: Boolean) {}
}

@Parcelize
class PhoneAuth @Inject constructor() : Parcelable {
    @IgnoredOnParcel
    private lateinit var activity: ComponentActivity
    @IgnoredOnParcel
    private lateinit var callbacksToHost: CallbacksFromPhoneAuthToHost

    companion object {
        private const val TAG = "PhoneAuthActivity"
    }

    fun setActivity(activity: ComponentActivity) {
        this.activity = activity
    }

    fun setCallbacks(callbacksToHost: CallbacksFromPhoneAuthToHost) {
        this.callbacksToHost = callbacksToHost
    }

    @IgnoredOnParcel
    private val auth: FirebaseAuth = Firebase.auth

    @IgnoredOnParcel
    private lateinit var storedVerificationId: String
    @IgnoredOnParcel
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    @IgnoredOnParcel
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

            if (e is FirebaseAuthInvalidCredentialsException) {
                callbacksToHost.notifyPhoneNumberException(e)
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
            callbacksToHost.notifyCodeSent(verificationId, token)
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
        callbacksToHost.notifyVerificationProgress(true)
    }

    fun onReceiveCodeToVerify(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)
        callbacksToHost.notifyVerificationProgress(false)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    callbacksToHost.notifySuccessfulLogin(user)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        callbacksToHost.notifyVerificationCodeException(task.exception as FirebaseAuthInvalidCredentialsException)

                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }
}