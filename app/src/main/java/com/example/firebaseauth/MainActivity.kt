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

    var verificationId by mutableStateOf("")
    var resendingToken by mutableStateOf(PhoneAuthProvider.ForceResendingToken.zza())
    var userLoggedIn by mutableStateOf(Firebase.auth.currentUser != null)
    private val self = this

    private val callbacks = object : CallbacksToHostFromPhoneAuth {
        override fun notifyCodeSent(
            verificationId: String,
            resendingToken: PhoneAuthProvider.ForceResendingToken
        ) {
            self.verificationId = verificationId
            self.resendingToken = resendingToken
        }

        override fun notifySuccessfulLogin(user: FirebaseUser?) {
            self.userLoggedIn = user != null
        }
    }

    val phoneAuth =
        PhoneAuth(self, callbacks)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)

        setContent {
            FirebaseAuthTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    HomeScreen(this)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(mainActivity: MainActivity) {
    var codeToVerify by remember {
        mutableStateOf("")
    }

    var phoneNumber by remember {
        mutableStateOf("+84")
    }

    if (mainActivity.userLoggedIn) {
        Column() {
            Text(text = "Welcome ${Firebase.auth.currentUser}")
            Button(onClick = {
                Firebase.auth.signOut(); mainActivity.userLoggedIn =
                false; mainActivity.verificationId = ""
            }) {
                Text(text = "Sign out")
            }
        }
    } else {
        if (mainActivity.verificationId.compareTo("") == 0) {
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
                        text = "Nh???p s??? ??i???n tho???i"
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
        modifier = modifier.width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(
            "M?? x??c th???c 6 s??? ???? ???????c g???i qua SMS.",
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
                    text = "Nh???p m?? x??c th???c",
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
                Text(text = "L???y m?? x??c th???c kh??c")
            }
            Button(onClick = { /*TODO*/ }, Modifier.fillMaxWidth()) {
                Text(text = "X??c th???c b???ng s??? ??i???n tho???i kh??c")
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