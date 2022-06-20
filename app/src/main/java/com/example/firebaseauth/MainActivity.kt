package com.example.firebaseauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity(), IPhoneAuth {
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putBoolean("VerificationInProgress", verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override var code: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)


        val phoneAuth =
            PhoneAuth(this) {
                onGetCodeFromUser { code = verifyCodeSentScreen() }
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
                        loginWithPhoneNumberScreen(phoneAuth)
                    } else {
                        Text(text = "Welcome $currentUser")
                    }
                }
            }
        }
    }
}

fun MainActivity.onGetCodeFromUser(verifyCodeSentScreen: @Composable () -> Unit): String {
    return code
}

@Composable
fun loginWithPhoneNumberScreen(auth: PhoneAuth?) {
    var phoneNumber by remember {
        mutableStateOf("+84")
    }
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.ic_group_2), null)
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { v -> phoneNumber = v },
                label = {
                    Text(
                        text = "Nhập số điện thoại"
                    )
                },
                keyboardActions = KeyboardActions(onDone = {
                    auth?.startPhoneNumberVerification(phoneNumber)
                }),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
            )
        }
    }
}

@Composable
fun verifyCodeSentScreen(): String {
    var inputCode by remember {
        mutableStateOf("")
    }
    TextField(
        value = inputCode,
        onValueChange = { v -> inputCode = v },
        keyboardActions = KeyboardActions(onDone = {
        }),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        label = { Text("Nhập code nhận được qua SMS") }
    )
    return inputCode
}

@Preview(showBackground = true)
@Composable
fun loginWithPasswordScreenPreview() {
    FirebaseAuthTheme {
        loginWithPhoneNumberScreen(null)
    }
}

@Preview(showBackground = true)
@Composable
fun verifyCodeSentScreenPreview() {
    FirebaseAuthTheme {
        verifyCodeSentScreen()
    }
}