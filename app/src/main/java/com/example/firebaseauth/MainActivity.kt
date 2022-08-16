package com.example.firebaseauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider

class MainActivity : ComponentActivity() {
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putBoolean("VerificationInProgress", verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    val authViewModel = AuthViewModel(AuthState())

    private val callbacks = object : CallbacksToHostFromPhoneAuth {
        override fun notifyCodeSent(
            verificationId: String,
            resendingToken: PhoneAuthProvider.ForceResendingToken
        ) {
            authViewModel.onCodeSent(verificationId, resendingToken)
        }

        override fun notifySuccessfulLogin(user: FirebaseUser?) {
            authViewModel.onSuccessfulLogin(user)
        }
    }

    val phoneAuth =
        PhoneAuth(this@MainActivity, callbacks)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
//        FirebaseAuth.getInstance().useEmulator("192.168.31.145", 9099)

        setContent {
            FirebaseAuthTheme {
                // A surface container using the 'background' color from the theme
                Surface(
//                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AuthHomeScreen(this@MainActivity, authViewModel)
                }
            }
        }
    }
}

@Composable
fun AuthHomeScreen(mainActivity: MainActivity, authViewModel: AuthViewModel) {
    val authState = authViewModel.authStateFlow.collectAsState()
    var codeToVerify by remember {
        mutableStateOf("")
    }

    var phoneNumber by remember {
        mutableStateOf("+84")
    }

    if (authState.value.userLoggedIn) {
        Column() {
            Text(text = "Welcome ${authViewModel.getCurrentUser()}")
            Button(onClick = authViewModel::logUserOut) {
                Text(text = "Sign out")
            }
        }
    } else {
        if (authState.value.verificationId.compareTo("") == 0) {
            LoginWithPhoneNumberScreen(
                phoneNumber,
                { phoneNumber = it },
                { mainActivity.phoneAuth.startPhoneNumberVerification(phoneNumber) })
        } else {
            VerifyCodeScreen(
                codeToVerify,
                { codeToVerify = it },
                { mainActivity.phoneAuth.onReceiveCodeToVerify(codeToVerify) })
        }
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
            .fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.ic_group_2), null)
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onValueChange,
                singleLine = true,
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

const val VERIFICATION_CODE_LENGTH = 6

@Composable
fun VerifyCodeScreen(
    codeToVerify: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(
            "Mã xác thực 6 số đã được gửi qua SMS.",
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
        )
        TextField(
            value = codeToVerify,
            onValueChange = {
                if (it.length <= VERIFICATION_CODE_LENGTH) {
                    onValueChange(it)
                }
                if (it.length == VERIFICATION_CODE_LENGTH) {
                    onDone()
                }
            },
//            modifier = Modifier.widthIn(max = 90.dp),
            textStyle = TextStyle(textAlign = TextAlign.Center),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.None
            ),
            label = {
                Text(
                    text = "Nhập mã xác thực",
//                    style = TextStyle(textAlign = TextAlign.Center)
                )
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Divider(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
//        Divider(Modifier.height(1.dp).background(Color.LightGray))
        Spacer(
            modifier = Modifier.height(5.dp)
        )
        Column(
            modifier = modifier
                .width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { /*TODO*/ }, Modifier.fillMaxWidth()) {
                Text(text = "Lấy mã xác thực khác")
            }
            Button(onClick = { /*TODO*/ }, Modifier.fillMaxWidth()) {
                Text(text = "Xác thực bằng số điện thoại khác")
            }
        }
    }

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