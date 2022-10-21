package com.example.firebaseauth.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebaseauth.CountryNamesAndDialCodes
import com.example.firebaseauth.R
import com.example.firebaseauth.SelectedCountry
import com.example.firebaseauth.auth.AuthActivity
import com.example.firebaseauth.data.CountryNamesAndCallingCodesModel
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AuthActivity.HomeContent() {
    FirebaseAuthTheme {
        val scaffoldState =
            rememberScaffoldState(snackbarHostState = authViewModel.snackbarHostState)

        val navController = rememberNavController()

        val activity = this

        Scaffold(scaffoldState = scaffoldState) {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
            ) {
                NavHost(navController = navController, startDestination = "AuthHomeScreen") {
                    composable(route = "AuthHomeScreen") {
                        AuthHomeScreen(
                            scaffoldState = scaffoldState,
                            onNavigateToCountryNamesAndCallingCodesScreen = {
                                navController.navigate(
                                    "CountryNamesAndCallingCodesScreen"
                                )
                            },
                            targetActivity = activity,
                        )
                    }
                    composable(route = "CountryNamesAndCallingCodesScreen") {
                        CountryNamesAndCallingCodesScreen {
                            navController.navigate(
                                "AuthHomeScreen"
                            ) { popUpTo("AuthHomeScreen") { inclusive = true } }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("RememberReturnType")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthHomeScreen(
    scaffoldState: ScaffoldState,
    onNavigateToCountryNamesAndCallingCodesScreen: () -> Unit,
    targetActivity: AuthActivity,
) {
    val vm = remember {
        targetActivity.authViewModel
    }
    val phoneAuth = remember {
        targetActivity.phoneAuth
    }

    val savedSelectedCountryState by vm.flowOfSavedSelectedCountry.collectAsState(
        initial = SelectedCountry.getDefaultInstance()
    )

    var authHomeUIState by remember {
        mutableStateOf(vm.authUIState.authHomeUIState)
    }

    var authRequestUIState by remember {
        mutableStateOf(vm.authUIState.authRequestUIState)
    }

    var authVerificationUIState by remember {
        mutableStateOf(vm.authUIState.authVerificationUIState)
    }

    LaunchedEffect(Unit) {
        vm.authUIStateFlow.collect {
            authHomeUIState = it.authHomeUIState
            authRequestUIState = it.authRequestUIState
            authVerificationUIState = it.authVerificationUIState
        }
    }

    when {
        authHomeUIState.shouldShowLandingScreen -> LandingScreen(isDoneProvider = { vm.countriesAndDialCodes.isNotEmpty() }) {
            vm.setShouldShowLandingScreen(false)
        }

        vm.connectionExceptionMessage.isNotEmpty() -> {
            Text(text = vm.connectionExceptionMessage)
        }

        authHomeUIState.userSignedIn -> {
            Column {
                val user = Firebase.auth.currentUser
                Text(text = "Welcome ${user?.displayName ?: user?.phoneNumber}")
                Button(onClick = vm::logUserOut) {
                    Text(text = "Sign out")
                }
            }
        }

        else -> {
            val kbController = LocalSoftwareKeyboardController.current

            when {
                authHomeUIState.verificationId.isEmpty() -> {
                    var phoneNumber by rememberSaveable {
                        mutableStateOf("")
                    }

                    LoginWithPhoneNumberScreen(
                        countryNamesAndDialCodes = vm.countriesAndDialCodes,
                        selectedCountryProvider = { savedSelectedCountryState },
                        onSelectedCountryChange = {
                            vm.saveSelectedCountry(it)
                            if (hasException(authRequestUIState.requestExceptionMessage)) vm.onRequestException(
                                ""
                            )
                        },
                        onNavigateToCountryNamesAndCallingCodesScreen = onNavigateToCountryNamesAndCallingCodesScreen,
                        phoneNumberProvider = { phoneNumber },
                        onPhoneNumberChange = {
                            if (hasException(authRequestUIState.requestExceptionMessage)) vm.onRequestException(
                                ""
                            )
                            phoneNumber = if (it.isNotEmpty() && it.last().code != KEY_ENTER)
                                it
                            else it
                        },
                        onDone = {
                            if (savedSelectedCountryState.container.dialCode.isEmpty()) vm.onEmptyDialCode()
                            else phoneAuth.startPhoneNumberVerification(
                                "${savedSelectedCountryState.container.dialCode}${phoneNumber.trimStart { it == '0' }}",
                                authHomeUIState.resendingToken
                            )
                            kbController?.hide()
                        },
                        requestInProgressProvider = { authRequestUIState.requestInProgress },
                        exceptionMessageProvider = { authRequestUIState.requestExceptionMessage },
                        handleLocationPermissionRequest = targetActivity::handleLocationPermissionRequest,
                        isRequestTimeoutProvider = { authRequestUIState.isRequestTimeout },
                        onRequestTimeout = vm::onRequestTimeout,
                        onRetry = { vm.dismissSnackbar(); vm.cancelPendingActiveListener();vm.logUserOut() },
                        snackbarHostState = scaffoldState.snackbarHostState
                    )
                }

                else -> {
                    var codeToVerify by rememberSaveable {
                        mutableStateOf("")
                    }

                    remember {
                        vm.dismissSnackbar()
                        vm.cancelPendingActiveListener()
                    }

                    VerifyCodeScreen(
                        codeToVerifyProvider = { codeToVerify },
                        onCodeChange = {
                            if (it.length <= VERIFICATION_CODE_LENGTH) {
                                if (hasException(authVerificationUIState.verificationExceptionMessage)) vm.onVerificationException(
                                    ""
                                )

                                if (it.last().code != KEY_ENTER) // Not key Enter
                                    codeToVerify = it
                            }

                            if (codeToVerify.length == VERIFICATION_CODE_LENGTH) {
                                phoneAuth.onReceiveCodeToVerify(
                                    authHomeUIState.verificationId, codeToVerify
                                )
                                kbController?.hide()
                            }
                        },
                        exceptionMessageProvider = { authVerificationUIState.verificationExceptionMessage },
                        verificationInProgressProvider = { authVerificationUIState.verificationInProgress },
                        scaffoldState.snackbarHostState,
                        isVerificationTimeoutProvider = { authVerificationUIState.isVerificationTimeout },
                        onVerificationTimeout = vm::onVerificationTimeout,
                        onRetry = { vm.logUserOut() },
                    )
                }
            }
        }
    }

    var shouldOpenLocationRequestPermissionRationaleDialog by vm::shouldOpenLocationRequestPermissionRationaleDialog
    if (shouldOpenLocationRequestPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                shouldOpenLocationRequestPermissionRationaleDialog = false
            },
            title = {
                Text(text = "Cho phép dùng Định vị?")
            },
            text = {
                Text(
                    "Bạn cần cho phép dùng Định vị để ứng dụng tự xác định quốc gia hiện tại bạn đang sử dụng " +
                            "điện thoai."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        shouldOpenLocationRequestPermissionRationaleDialog = false
                        targetActivity.locationPermissionRequest.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("Tiếp tục")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        shouldOpenLocationRequestPermissionRationaleDialog = false
                    }
                ) {
                    Text("Không")
                }
            }
        )
    }

}

const val KEY_ENTER = 10

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoginWithPhoneNumberScreen(
    countryNamesAndDialCodes: List<CountryNamesAndDialCodes.NameAndDialCode>,
    selectedCountryProvider: () -> SelectedCountry,
    onSelectedCountryChange: (CountryNamesAndDialCodes.NameAndDialCode) -> Unit,
    onNavigateToCountryNamesAndCallingCodesScreen: () -> Unit,
    phoneNumberProvider: () -> String,
    onPhoneNumberChange: (String) -> Unit,
    onDone: () -> Unit,
    requestInProgressProvider: () -> Boolean,
    exceptionMessageProvider: () -> String,
    handleLocationPermissionRequest: () -> Unit,
    isRequestTimeoutProvider: () -> Boolean,
    onRequestTimeout: () -> Unit,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Surface(
        color = MaterialTheme.colors.background, modifier = Modifier.fillMaxWidth()
    ) {
        val selectedCountry = selectedCountryProvider()
        val phoneNumber = phoneNumberProvider()
        val requestInProgress = requestInProgressProvider()
        val exceptionMessage = exceptionMessageProvider()

        var expanded by rememberSaveable {
            mutableStateOf(false)
        }

        var selectedCountryName by rememberSaveable {
            mutableStateOf("")
        }

        var lastSelectedCountryDialCode by rememberSaveable {
            mutableStateOf("")
        }

        if (!lastSelectedCountryDialCode.contentEquals(selectedCountry.container.dialCode)) {
            selectedCountryName = selectedCountry.container.name
            SideEffect {
                lastSelectedCountryDialCode = selectedCountry.container.dialCode
            }
        }

        val horizontalCenterColumnWidth = 280.dp
        var yOfProgressionSurface = 0
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(horizontalCenterColumnWidth)
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            yOfProgressionSurface = placeable.height
                            layout(placeable.width, placeable.height) {
                                placeable.placeRelative(0, 0)
                            }
                        }
                        .padding(top = 30.dp)
                ) {
                    /*Image(
                        painter = painterResource(id = R.drawable.logo),
                        null,
                        modifier = Modifier.padding(top = 50.dp)
                    )*/
                    Box {
                        var textFieldHeight by remember {
                            mutableStateOf(0)
                        }

                        var getHeightDone by remember {
                            mutableStateOf(false)
                        }

                        if (!getHeightDone) {
                            OutlinedTextField(
                                "",
                                onValueChange = {},
                                modifier = Modifier.layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    textFieldHeight = placeable.height
                                    layout(placeable.width, placeable.height) {}
                                })

                            getHeightDone = true
                        }

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = onPhoneNumberChange,
                            placeholder = {
                                Text(
                                    text = "Số điện thoại"
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .height(with(LocalDensity.current) { textFieldHeight.toDp() }),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .padding(end = 5.dp)
                                            .clickable(onClick = { onNavigateToCountryNamesAndCallingCodesScreen() }),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "VN",
                                            modifier = Modifier
                                                .padding(start = 10.dp),
                                            color = MaterialTheme.colors.secondaryVariant
                                        )

                                        Image(
                                            painter = painterResource(id = R.drawable.ic_outline_arrow_drop_down_24),
                                            contentDescription = null,
                                        )

                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(3.dp)
                                                .padding(start = 2.dp, top = 1.dp, bottom = 1.dp)
                                                .background(
                                                    color = MaterialTheme.colors.primaryVariant.copy(
                                                        alpha = .3f
                                                    )
                                                )
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardActions = KeyboardActions(onDone = { onDone() }),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done
                            ),
                        )
                    }

                    Button(
                        modifier = Modifier.padding(top = 18.dp),
                        onClick = { handleLocationPermissionRequest() /* not using function reference for the sake of avoiding recomposition */ }
                    ) { Text(text = "Tự động xác định quốc gia từ vị trí") }
                }

                if (hasException(exceptionMessage)) {
                    ExceptionShowBox(exceptionMessage = exceptionMessage)
                } else if (!requestInProgress) {
                    val countryJson = stringArrayResource(id = R.array.countries)
                    Column() {
                        Button(
                            onClick = { onDone() /*indirect call to avoid recomposition*/ },
                            modifier = Modifier
                                .width(horizontalCenterColumnWidth)
                                .padding(top = 24.dp)
                        ) { Text(text = "Xong") }

                        val scope = rememberCoroutineScope()
                        Button(
                            onClick = {
                                // using coroutines here causing recomposition.
                                scope.launch {
                                    try {
                                        val json = Json { ignoreUnknownKeys = true }

                                        val country =
                                            mutableListOf<List<CountryNamesAndCallingCodesModel>>()
                                        countryJson.forEach {
                                            country.add(
                                                json.decodeFromString<List<CountryNamesAndCallingCodesModel>>(
                                                    it
                                                )
                                            )
                                        }
                                        var count = 0
                                        country.forEach { c -> c.forEach { count++; Timber.d("$count: ${it.name}") } }
                                    } catch (exc: Exception) {
                                        Timber.e(exc.message)
                                    }
                                }
                            },
                            modifier = Modifier
                                .width(horizontalCenterColumnWidth)
                                .padding(top = 24.dp)
                        ) { Text(text = "Do") }
                    }
                }
            }

            if (requestInProgress) {
                val isRequestTimeout = isRequestTimeoutProvider()
                if (isRequestTimeout) {
                    LaunchedEffect(key1 = phoneNumber) {
                        showNoticeAndRecommendation(
                            snackbarHostState,
                            onRetry
                        )
                    }
                } else
                    LaunchedEffect(key1 = phoneNumber) {
                        delay(TIME_THRESHOLD_FOR_RESPONSE)
                        onRequestTimeout()
                        showNoticeAndRecommendation(
                            snackbarHostState,
                            onRetry
                        )
                    }

                Surface(
                    color = Color.Transparent.copy(0.37f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Surface(
                        modifier = Modifier
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                layout(constraints.maxWidth, constraints.maxHeight) {
                                    placeable.placeRelative(0, yOfProgressionSurface)
                                }
                            }
                            .fillMaxWidth()
                            .padding(10.dp)
                            .padding(top = 8.dp, bottom = 8.dp),
                        elevation = 2.dp,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(
                            Modifier
                                .padding(10.dp)
                                .width(IntrinsicSize.Max)
                                .align(Alignment.Center)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
//                    Divider(Modifier.height(3.dp))
                            Text(
                                text = "Chờ mã xác minh",
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}

const val TIME_THRESHOLD_FOR_RESPONSE = 30000L

suspend fun showNoticeAndRecommendation(
    snackbarHostState: SnackbarHostState,
    doAsRecommended: () -> Unit
) = coroutineScope {
    val result = snackbarHostState.showSnackbar(
        "Thời gian phản hồi lâu hơn dự kiến.",
        "Đăng nhập khác",
        SnackbarDuration.Indefinite
    )
    if (result == SnackbarResult.ActionPerformed) {
        doAsRecommended()
    }
}

fun constraintsWithNewMaxWidth(constraints: Constraints, newMaxWith: Int): Constraints =
    Constraints(
        minWidth = constraints.minWidth,
        maxWidth = newMaxWith,
        minHeight = constraints.minHeight,
        maxHeight = constraints.maxHeight
    )

fun Modifier.layoutWithNewMaxWidth(newMaxWith: Int): Modifier =
    layout { measurable, constraints ->
        val component = measurable.measure(
            constraintsWithNewMaxWidth(constraints, newMaxWith)
        )
        layout(
            component.width, component.height
        ) { component.placeRelative(0, 0) }
    }

fun hasException(message: String?) = !message.isNullOrEmpty()

const val VERIFICATION_CODE_LENGTH = 6

@Composable
fun VerifyCodeScreen(
    codeToVerifyProvider: () -> String,
    onCodeChange: (String) -> Unit,
    exceptionMessageProvider: () -> String,
    verificationInProgressProvider: () -> Boolean,
    snackbarHostState: SnackbarHostState,
    isVerificationTimeoutProvider: () -> Boolean,
    onVerificationTimeout: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val codeToVerify = codeToVerifyProvider()
    val exceptionMessage = exceptionMessageProvider()
    val verificationInProgress = verificationInProgressProvider()
    var yProgressionSurface = 0

    Box {
        Column(
            modifier = modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    yProgressionSurface = placeable.height
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }
                .width(IntrinsicSize.Max)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Mã xác thực 6 số đã được gửi qua SMS.",
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
            )

            TextField(value = codeToVerify,
                onValueChange = onCodeChange,
                textStyle = TextStyle(textAlign = TextAlign.Center),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.None
                ),
                label = {
                    Text(
                        text = "Nhập mã xác thực",
//                    style = TextStyle(textAlign = TextAlign.Center)
                    )
                })

            Spacer(modifier = Modifier.height(10.dp))
            Divider(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            )
//        Divider(Modifier.height(1.dp).background(Color.LightGray))
            /*Spacer(
                modifier = Modifier.height(5.dp)
            )*/

            if (hasException(exceptionMessage)) {
                ExceptionShowBox(exceptionMessage = exceptionMessage)
            }
        }
        if (verificationInProgress) {
            val isVerificationTimeout = isVerificationTimeoutProvider()
            if (isVerificationTimeout) {
                LaunchedEffect(key1 = codeToVerify) {
                    showNoticeAndRecommendation(
                        snackbarHostState,
                        onRetry
                    )
                }
            } else LaunchedEffect(key1 = codeToVerify) {
                delay(TIME_THRESHOLD_FOR_RESPONSE)
                onVerificationTimeout()
                showNoticeAndRecommendation(
                    snackbarHostState,
                    onRetry
                )
            }

            Surface(
                color = Color.Transparent.copy(0.37f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Surface(
                    modifier = Modifier
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(constraints.maxWidth, constraints.maxHeight) {
                                placeable.placeRelative(0, yProgressionSurface)
                            }
                        }
                        .fillMaxWidth()
                        .padding(10.dp)
                        .padding(top = 8.dp, bottom = 8.dp),
                    elevation = 2.dp,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        Modifier
                            .padding(10.dp)
                            .width(IntrinsicSize.Max)
                            .align(Alignment.Center)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
//                    Divider(Modifier.height(3.dp))
                        Text(
                            text = "Đang xác thực",
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginWithPasswordScreenPreview() {
    FirebaseAuthTheme {
        LoginWithPhoneNumberScreen(
            countryNamesAndDialCodes = listOf(),
            selectedCountryProvider = { SelectedCountry.getDefaultInstance() },
            onSelectedCountryChange = {},
            onNavigateToCountryNamesAndCallingCodesScreen = {},
            phoneNumberProvider = { "" },
            onPhoneNumberChange = {},
            onDone = {},
            requestInProgressProvider = { false },
            exceptionMessageProvider = { "" },
            handleLocationPermissionRequest = {},
            isRequestTimeoutProvider = { false },
            onRequestTimeout = {},
            onRetry = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VerifyCodeScreenPreview() {
    FirebaseAuthTheme {
        VerifyCodeScreen({ "" }, {}, { "" }, { false }, SnackbarHostState(), { false }, {}, {})
    }
}

@Composable
fun ExceptionShowBox(exceptionMessage: String) {
    Surface(
        modifier = Modifier
            .padding(10.dp)
            .wrapContentSize(),
        color = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.error,
        elevation = 2.dp
    ) {
        Text(
            text = exceptionMessage,
            Modifier.padding(10.dp),
        )
    }
}
