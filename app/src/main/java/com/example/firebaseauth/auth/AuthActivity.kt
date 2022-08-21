package com.example.firebaseauth.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebaseauth.R
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)

        setContent {
            FirebaseAuthTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val authViewModel = viewModel<AuthViewModel>()
                    val callbacks = remember {
                        object : CallbacksToHostFromPhoneAuth {
                            override fun notifyCodeSent(
                                verificationId: String,
                                resendingToken: PhoneAuthProvider.ForceResendingToken
                            ) {
                                authViewModel.onCodeSent(verificationId, resendingToken)
                            }

                            override fun notifySuccessfulLogin(user: FirebaseUser?) {
                                authViewModel.onSuccessfulLogin(user)
                            }

                            override fun notifyPhoneNumberException(exception: FirebaseAuthInvalidCredentialsException) {
                                authViewModel.onPhoneNumberException(exception)
                            }

                            override fun notifyVerificationCodeException(exception: FirebaseAuthInvalidCredentialsException) {
                                authViewModel.onVerificationCodeException(exception)
                            }

                            override fun notifyVerificationProgress(progress: Boolean) {
                                authViewModel.onVerificationProgressNotification(progress)
                            }

                            override fun notifyLoggingProgress(progress: Boolean) {
                                TODO("Not yet implemented")
                            }
                        }
                    }
                    val phoneAuth = remember {
                        PhoneAuth().apply { setActivity(this@AuthActivity);setCallbacks(callbacks) }
                    }
                    AuthHomeScreen(authViewModel, phoneAuth)
                }
            }
        }
    }
}

@Composable
fun AuthHomeScreen(authViewModel: AuthViewModel, phoneAuth: PhoneAuth) {
    val authStateFromFlow by authViewModel.authStateFlow.collectAsState()
    var codeToVerify by remember {
        mutableStateOf("")
    }

    var phoneNumber by remember {
        mutableStateOf("+84")
    }

    fun hasUserLoggedIn() = authStateFromFlow.user != null
    if (hasUserLoggedIn()) {
        codeToVerify = ""
        Column() {
            Text(text = "Welcome ${authStateFromFlow.user}")
            Button(onClick = authViewModel::logUserOut) {
                Text(text = "Sign out")
            }
        }
    } else {
        if (authStateFromFlow.verificationId.compareTo("") == 0) {
            LoginWithPhoneNumberScreen(
                phoneNumber,
                { phoneNumber = it },
                {
                    phoneAuth.startPhoneNumberVerification(
                        phoneNumber,
                        authStateFromFlow.resendingToken
                    )
                }, authStateFromFlow.waitingForVerificationCode
            )
        } else {
            VerifyCodeScreen(
                codeToVerify,
                { codeToVerify = it },
                { phoneAuth.onReceiveCodeToVerify(codeToVerify) },
                authStateFromFlow.verificationCodeException,
                authViewModel::clearVerificationCodeException
            )
        }
    }
}

@Composable
fun LoginWithPhoneNumberScreen(
    phoneNumber: String,
    onValueChange: (String) -> Unit,
    onDone: KeyboardActionScope.() -> Unit,
    waitingForVerificationCode: Boolean
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

            if (waitingForVerificationCode) {
                Column {
                    CircularProgressIndicator()
                    Divider(Modifier.height(3.dp))
                    Text(text = "Chờ mã xác minh")
                }
            }
        }
    }
}

const val VERIFICATION_CODE_LENGTH = 6

@Composable
fun VerifyCodeScreen(
    codeToVerify: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    exception: FirebaseAuthException? = null,
    clearException: () -> Unit,
    modifier: Modifier = Modifier,
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
        fun hasException() = exception != null
        TextField(
            value = codeToVerify,
            onValueChange = {
                if (it.length <= VERIFICATION_CODE_LENGTH) {
                    if (hasException()) clearException()
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
        /* Column(
             modifier = modifier
                 .width(IntrinsicSize.Max),
             horizontalAlignment = Alignment.CenterHorizontally
         ) {
             Button(onClick = {  }, Modifier.fillMaxWidth()) {
                Text(text = "Xác thực bằng số điện thoại khác")
            }
        }*/
        if (hasException()) {
            Box(
                Modifier
                    .padding(10.dp)
                    .border(BorderStroke(3.dp, Color.Blue))
                    .wrapContentSize()
            ) {
                Text(text = exception?.message!!, Modifier.padding(10.dp))
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun LoginWithPasswordScreenPreview() {
    FirebaseAuthTheme {
        LoginWithPhoneNumberScreen("+84", {}, {}, false)
    }
}

@Preview(showBackground = true)
@Composable
fun VerifyCodeScreenPreview() {
    FirebaseAuthTheme {
        VerifyCodeScreen("", {}, {}, null, {})
    }
}
