package com.example.firebaseauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putBoolean("VerificationInProgress", verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().useEmulator("10.0.0.2", 9099)

        val phoneAuth =
            PhoneAuth(this) { verificationId, token ->
                onVerifyCodeSent(
                    verificationId,
                    token
                ) { verifyCodeSentScreen(code = verificationId) }
            }
        val currentUser = phoneAuth.auth.currentUser
        setContent {
            FirebaseAuthTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    if (currentUser == null) {
                        loginWithPhoneNumberScreen(phoneAuth.auth)
                    } else {
                        Text(text = "Welcome $currentUser")
                    }
                }
            }
        }
    }
}

fun onVerifyCodeSent(
    verificationId: String,
    token: PhoneAuthProvider.ForceResendingToken,
    verifyCodeSentScreen: @Composable (code: String) -> Unit
) {
}

@Composable
fun loginWithPhoneNumberScreen(auth: FirebaseAuth = Firebase.auth) {
    Surface(
        color = Color.Black,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.wrapContentSize()) {
            Image(painter = painterResource(id = R.drawable.ic_group_2), null)
//            TextField(value = , onValueChange = )
        }
    }
}

@Composable
fun loginWithPhoneNumberScreenPrototype() {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.wrapContentSize()) {
            Image(painter = painterResource(id = R.drawable.ic_group_2), null)
//            TextField(value = , onValueChange = )
//            Text(text = "ABC", modifier = Modifier.width(239.dp).wrapContentWidth())
        }
    }
}

@Composable
fun verifyCodeSentScreen(code: String) {
}

@Preview(showBackground = true)
@Composable
fun loginWithPasswordScreenPreview() {
    FirebaseAuthTheme {
        loginWithPhoneNumberScreenPrototype()
    }
}