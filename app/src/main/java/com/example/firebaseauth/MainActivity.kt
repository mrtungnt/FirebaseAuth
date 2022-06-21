package com.example.firebaseauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    private var numOfReceivingCode by mutableStateOf(0)
    private var currentUser by mutableStateOf(Firebase.auth.currentUser)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)

        val phoneAuth =
            PhoneAuth(this, { numOfReceivingCode++ }, { user -> currentUser = user })

        setContent {
            FirebaseAuthTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    HomeScreen(currentUser, phoneAuth, numOfReceivingCode = numOfReceivingCode)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(user: FirebaseUser?, phoneAuth: PhoneAuth, numOfReceivingCode: Int) {
    var respondedCode by remember {
        mutableStateOf("")
    }

    var phoneNumber by remember {
        mutableStateOf("+84")
    }

    if (user == null) {
        if (numOfReceivingCode == 0) {
            LoginWithPhoneNumberScreen(
                phoneNumber,
                { v -> phoneNumber = v },
                { phoneAuth.startPhoneNumberVerification(phoneNumber) })
        } else {
            VerifyCodeScreen(
                respondedCode,
                { v -> respondedCode = v },
                { phoneAuth.onReceiveRespondedCode(respondedCode) })
        }
    } else {
        Text(text = "Welcome $user")
    }
}

@Composable
fun LoginWithPhoneNumberScreen(
    phoneNumber: String,
    onValueChange: (String) -> Unit,
    onDone: KeyboardActionScope.() -> Unit
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.ic_group_2), null)
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onValueChange,
                label = {
                    Text(
                        text = "Nhập số điện thoại"
                    )
                },
                keyboardActions = KeyboardActions(onDone = onDone),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
            )
        }
    }
}

@Composable
fun VerifyCodeScreen(
    codeToVerify: String,
    onValueChange: (String) -> Unit,
    onDone: KeyboardActionScope.() -> Unit
) {

    TextField(
        value = codeToVerify,
        onValueChange = onValueChange,
        keyboardActions = KeyboardActions(onDone = onDone),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        label = { Text("Nhập code nhận được qua SMS") }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginWithPasswordScreenPreview() {
    FirebaseAuthTheme {
        LoginWithPhoneNumberScreen("+84", {}, {})
    }
}

@Preview(showBackground = true)
@Composable
fun VerifyCodeScreenPreview() {
    FirebaseAuthTheme {
        VerifyCodeScreen("", {}, {})
    }
}