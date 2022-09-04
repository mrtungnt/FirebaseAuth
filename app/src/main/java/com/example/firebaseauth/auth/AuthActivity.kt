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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebaseauth.CountriesAndDialCodes
import com.example.firebaseauth.R
import com.example.firebaseauth.data.network.CountryAndDialCodeModelRemote
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

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
                        object : CallbacksFromPhoneAuthToHost() {
                            override fun onCodeSent(
                                verificationId: String,
                                resendingToken: PhoneAuthProvider.ForceResendingToken
                            ) {
                                authViewModel.onCodeSent(verificationId, resendingToken)
                            }

                            override fun onSuccessfulLogin() {
                                authViewModel.onSuccessfulLogin()
                            }

                            override fun onRequestException(exceptionMessage: String) {
                                authViewModel.onRequestException(exceptionMessage)
                            }

                            override fun onVerificationException(exceptionMessage: String) {
                                authViewModel.onVerificationException(exceptionMessage)
                            }

                            override fun onVerificationInProgress(inProgress: Boolean) {
                                authViewModel.onVerificationInProgress(inProgress)
                            }

                            override fun onRequestInProgress(inProgress: Boolean) {
                                authViewModel.onRequestInProgress(inProgress)
                            }

                            /* override fun onLoggingProgress(progress: Boolean) {
                                 TODO("Not yet implemented")
                             }*/
                        }
                    }

                    val phoneAuth = remember {
                        PhoneAuth(
                            activity = this@AuthActivity,
                            callbacks,
                        )
                    }
                    AuthHomeScreen(
                        authViewModel,
                        phoneAuth,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthHomeScreen(
    authViewModel: AuthViewModel,
    phoneAuth: PhoneAuth,
) {
    val authState by authViewModel.authStateFlow.collectAsState()

    var codeToVerify by rememberSaveable {
        mutableStateOf("")
    }

    var phoneNumber by rememberSaveable {
        mutableStateOf("")
    }

    var selectedCountry by rememberSaveable {
        mutableStateOf(
            CountriesAndDialCodes.CountryAndDialCode.newBuilder().setName("").setDialCode("")
                .build()
        )
    }

    val kbController = LocalSoftwareKeyboardController.current

    fun hasUserLoggedIn() = authState.userSignedIn

    when {
        hasUserLoggedIn() -> {
            codeToVerify = ""
            Column() {
                val user = Firebase.auth.currentUser
                Text(text = "Welcome ${user?.displayName ?: user?.phoneNumber}")
                Button(onClick = authViewModel::logUserOut) {
                    Text(text = "Sign out")
                }
            }
        }

//        authViewModel.countriesAndDialCodes.isEmpty() -> {
        /*authViewModel.countriesAndDialCodes.first().dataList.isEmpty() -> {
            Text("Initializing")
        }*/

        !authViewModel.connectionExceptionMessage.isNullOrEmpty() -> {
            Text(text = authViewModel.connectionExceptionMessage!!)
        }

        else -> {
            when {
                authState.verificationId.isEmpty() -> {
                    LoginWithPhoneNumberScreen(
                        authViewModel.countriesAndDialCodes,
                        selectedCountry,
                        {
                            selectedCountry = it
                            if (hasException(authState.requestExceptionMessage))
                                authViewModel.clearRequestExceptionMessage()
                        },
                        phoneNumber,
                        {
                            if (hasException(authState.requestExceptionMessage))
                                authViewModel.clearRequestExceptionMessage()
                            phoneNumber = it
                        },
                        {
                            if (selectedCountry.dialCode.isEmpty())
                                authViewModel.onEmptyDialCode()
                            else
                                phoneAuth.startPhoneNumberVerification(
                                    "${selectedCountry.dialCode}${phoneNumber.trimStart { it == '0' }}",
                                    authState.resendingToken
                                )
//                kbController?.hide()
                        },
                        authState.requestInProgress,
                        authState.requestExceptionMessage,
                    )
                }

                else -> {
                    VerifyCodeScreen(
                        codeToVerify,
                        {
                            if (it.length <= VERIFICATION_CODE_LENGTH) {
                                if (hasException(authState.verificationExceptionMessage))
                                    authViewModel.clearVerificationExceptionMessage()

                                if (it.last().code != 10) // Not key Enter
                                    codeToVerify = it
                            }

                            if (codeToVerify.length == VERIFICATION_CODE_LENGTH) {
                                phoneAuth.onReceiveCodeToVerify(
                                    authState.verificationId,
                                    codeToVerify
                                )
                            }
                        },
                        authState.verificationExceptionMessage,
                        authState.verificationInProgress,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoginWithPhoneNumberScreen(
    countriesAndDialCodes: List<CountriesAndDialCodes.CountryAndDialCode>,
    selectedCountry: CountriesAndDialCodes.CountryAndDialCode,
    onSelectedCountryChange: (CountriesAndDialCodes.CountryAndDialCode) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onDone: KeyboardActionScope.() -> Unit,
    requestInProgress: Boolean,
    exceptionMessage: String?,
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        var expanded by rememberSaveable {
            mutableStateOf(false)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.ic_group_2), null)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }) {
                TextField(
                    value = selectedCountry.name,
                    onValueChange = {
                        onSelectedCountryChange(
                            selectedCountry.toBuilder().setName(it).build()
                        )
                    },
                    label = { Text(text = "Quốc gia") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    }, colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                val filter = countriesAndDialCodes.filter {
                    it.name.contains(
                        selectedCountry.name,
                        ignoreCase = true
                    )
                }
                if (filter.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        filter.forEach {
                            DropdownMenuItem(onClick = {
                                onSelectedCountryChange(it)
                                expanded = false
                            }) {
                                Text(text = it.name)
                            }
                        }
                    }
                }
            }

            Row {
                Spacer(modifier = Modifier.weight(.142f))
                OutlinedTextField(
                    value = selectedCountry.dialCode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = "Mã QG") },
                    modifier = Modifier
                        .weight(.25f)
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
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
                    ),
                    modifier = Modifier
                        .weight(.45f)
                )
                Spacer(modifier = Modifier.weight(.142f))
            }

            if (hasException(exceptionMessage)) {
                Box(
                    Modifier
                        .padding(10.dp)
                        .border(BorderStroke(3.dp, Color.Blue))
                        .wrapContentSize()
                ) {
                    Text(text = exceptionMessage!!, Modifier.padding(10.dp))
                }
            } else if (requestInProgress) {
                var message by rememberSaveable() {
                    mutableStateOf("Chờ mã xác minh")
                }
                LaunchedEffect(key1 = phoneNumber) {
                    delay(10000)
                    message = "Thời gian đợi hơi lâu."
                }
                Column(
                    Modifier
                        .padding(5.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
//                    Divider(Modifier.height(3.dp))
                    Text(text = message, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

fun hasException(message: String?) = !message.isNullOrEmpty()

const val VERIFICATION_CODE_LENGTH = 6

@Composable
fun VerifyCodeScreen(
    codeToVerify: String,
    onCodeChange: (String) -> Unit,
    exceptionMessage: String? = null,
    verificationInProgress: Boolean,
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
        TextField(
            value = codeToVerify,
            onValueChange = onCodeChange,
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
        if (hasException(exceptionMessage)) {
            Box(
                Modifier
                    .padding(10.dp)
                    .border(BorderStroke(3.dp, Color.Blue))
                    .wrapContentSize()
            ) {
                Text(text = exceptionMessage!!, Modifier.padding(10.dp))
            }
        } else if (verificationInProgress) {
            Column(
                Modifier
                    .padding(5.dp)
                    .width(IntrinsicSize.Max)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
//                    Divider(Modifier.height(3.dp))
                Text(text = "Đang xác thực", textAlign = TextAlign.Center)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginWithPasswordScreenPreview() {
    FirebaseAuthTheme {
        LoginWithPhoneNumberScreen(
            listOf(),
            CountriesAndDialCodes.CountryAndDialCode.getDefaultInstance(),
            {},
            "",
            {},
            {},
            false,
            null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VerifyCodeScreenPreview() {
    FirebaseAuthTheme {
        VerifyCodeScreen("", {}, null, false)
    }
}
