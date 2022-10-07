package com.example.firebaseauth.auth

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.example.firebaseauth.CountryNamesAndDialCodes
import com.example.firebaseauth.SelectedCountry
import com.example.firebaseauth.forked.ForkedExposedDropdownMenuBox
import com.example.firebaseauth.ui.LandingScreen
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    val authViewModel by viewModels<AuthViewModel>()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(
                Manifest.permission.ACCESS_FINE_LOCATION, false
            ) -> {
                // Precise location access granted.
                Timber.d("ACCESS_FINE_LOCATION granted as requested.")
                whenLocationPermissionGranted()
            }

            permissions.getOrDefault(
                Manifest.permission.ACCESS_COARSE_LOCATION, false
            ) -> {
                // Only approximate location access granted.
                Timber.d("ACCESS_COARSE_LOCATION granted as requested.")
                whenLocationPermissionGranted()
            }

            else -> {
                // No location access granted.
                Timber.d("ACCESS_LOCATION not granted.")
            }
        }
    }

    private val intentSenderForEnablingLocation =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult())
        { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                whenLocationReady()
            }
        }

    fun handleLocationPermissionRequest() {
        val permissionCheck = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        when {
            permissionCheck == PackageManager.PERMISSION_GRANTED -> {
                Timber.d(
                    "ACCESS_COARSE_LOCATION granted."
                )
                whenLocationPermissionGranted()
            }

            shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                authViewModel.updateSnackbar(
                    "Hãy cho phép dùng Định vị (Location) để sử dụng tính năng này.",
                    duration = SnackbarDuration.Short
                )
            }

            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private suspend fun getFromLocation(location: Location): List<Address> {
        return withContext(context = authViewModel.viewModelScope.coroutineContext + Dispatchers.IO) {
            try {
                Geocoder(applicationContext).getFromLocation(
                    location.latitude, location.longitude, 1
                )
            } catch (exc: java.lang.Exception) {
                Timber.e("${exc.message}")
                emptyList()
            }
        }
    }

    private fun whenLocationReady() {
        authViewModel.updateSnackbar(
            "Đang xác định quốc gia từ vị trí. Trong một số điều kiện, có thể mất 30 giây.",
            SnackbarDuration.Indefinite
        )
        authViewModel.locationTask = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, authViewModel.locationCancellationToken
        )
        authViewModel.locationTask?.addOnSuccessListener {
            if (it != null) {
                authViewModel.viewModelScope.launch {
                    val address = getFromLocation(it)
                    if (address.isNotEmpty()) {
                        authViewModel.setSelectedCountry(address.first().countryName)
                        authViewModel.updateSnackbar("Đã xác định quốc gia từ vị trí.")
                    }
                }
            } else {
                authViewModel.updateSnackbar("Không xác định được vị trí.")
            }
        }
        authViewModel.locationTask?.addOnFailureListener {
            authViewModel.updateSnackbar(message = it.message!!)
        }
    }

    private fun whenLocationPermissionGranted() {
        val locationSettingsRequestBuilder =
            LocationSettingsRequest.Builder().addLocationRequest(LocationRequest.create())
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)
        val taskLocationSettingsResponse: Task<LocationSettingsResponse> =
            settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build())

        taskLocationSettingsResponse.addOnSuccessListener {
            whenLocationReady()
        }
        taskLocationSettingsResponse.addOnFailureListener {
            if (it is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling intentSenderForEnablingLocation.launch((),
                    // and check the result in the callback of registerForActivityResult().
                    val status = it.status
                    if (status.hasResolution()) {
                        val pendingIntent: PendingIntent = status.resolution!!
//                        Preconditions.checkNotNull(pendingIntent)
                        intentSenderForEnablingLocation.launch(
                            IntentSenderRequest.Builder(pendingIntent.intentSender)/*.setFillInIntent(null)
                                .setFlags(0, 0)*/.build()
                        )
                    }
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    Timber.e(
                        "${it.message}"
                    )
                }
            }
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var isConnected by mutableStateOf(false)

    private val callbacks = object : CallbacksFromPhoneAuthToHost() {
        override fun onCodeSent(
            verificationId: String, resendingToken: PhoneAuthProvider.ForceResendingToken
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

        /*override fun onLoggingProgress(progress: Boolean) {
            TODO("Not yet implemented")
        }*/
    }

    val phoneAuth = PhoneAuth(
        activity = this@AuthActivity,
        callbacks,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.registerNetworkCallback(
            NetworkRequest.Builder().build(), NetworkCallbackExt(this)
        )

//        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {

            if (isConnected) HomeContent()
            else NoConnectionDisplay()
        }
    }
}

@Composable
fun AuthActivity.HomeContent() {
    FirebaseAuthTheme {
        val scaffoldState =
            rememberScaffoldState(snackbarHostState = authViewModel.snackbarHostState)
        Scaffold(scaffoldState = scaffoldState) {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
            ) {
                AuthHomeScreen(
                    scaffoldState,
                    authViewModel.authStateFlow,
                    this,
                )
            }
        }
    }
}

@Composable
fun NoConnectionDisplay() {
    Text(text = "No internet connection. Waiting for connection...")
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthHomeScreen(
    scaffoldState: ScaffoldState,
    authUIStateFlow: StateFlow<AuthUIState>,
    targetActivity: AuthActivity,
) {
    val savedSelectedCountryState by targetActivity.authViewModel.flowOfSavedSelectedCountry.collectAsState(
        initial = SelectedCountry.getDefaultInstance()
    )

    var authHomeUIState by remember {
        mutableStateOf(targetActivity.authViewModel.authState.authHomeUIState)
    }

    var snackbarUIState by remember {
        mutableStateOf(targetActivity.authViewModel.authState.snackbarUIState)
    }

    var authRequestUIState by remember {
        mutableStateOf(targetActivity.authViewModel.authState.authRequestUIState)
    }

    var authVerificationUIState by remember {
        mutableStateOf(targetActivity.authViewModel.authState.authVerificationUIState)
    }

    LaunchedEffect(Unit) {
        authUIStateFlow.collect {
            authHomeUIState = it.authHomeUIState
            snackbarUIState = it.snackbarUIState
            authRequestUIState = it.authRequestUIState
            authVerificationUIState = it.authVerificationUIState
        }
    }

    when {
        authHomeUIState.shouldShowLandingScreen -> LandingScreen(isDoneProvider = { targetActivity.authViewModel.countriesAndDialCodes.isNotEmpty() }) {
            targetActivity.authViewModel.setShouldShowLandingScreen(false)
        }

        targetActivity.authViewModel.connectionExceptionMessage.isNotEmpty() -> {
            Text(text = targetActivity.authViewModel.connectionExceptionMessage)
        }

        authHomeUIState.userSignedIn -> {
            Column {
                val user = Firebase.auth.currentUser
                Text(text = "Welcome ${user?.displayName ?: user?.phoneNumber}")
                Button(onClick = targetActivity.authViewModel::logUserOut) {
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
                        countryNamesAndDialCodes = targetActivity.authViewModel.countriesAndDialCodes,
                        selectedCountryProvider = { savedSelectedCountryState },
                        onSelectedCountryChange = {
                            targetActivity.authViewModel.saveSelectedCountry(it)
                            if (hasException(authRequestUIState.requestExceptionMessage)) targetActivity.authViewModel.onRequestException(
                                ""
                            )
                        },
                        phoneNumberProvider = { phoneNumber },
                        onPhoneNumberChange = {
                            if (hasException(authRequestUIState.requestExceptionMessage)) targetActivity.authViewModel.onRequestException(
                                ""
                            )
                            if (it.last().code != KEY_ENTER)
                                phoneNumber = it
                        },
                        onDone = {
                            if (savedSelectedCountryState.container.dialCode.isEmpty()) targetActivity.authViewModel.onEmptyDialCode()
                            else targetActivity.phoneAuth.startPhoneNumberVerification(
                                "${savedSelectedCountryState.container.dialCode}${phoneNumber.trimStart { it == '0' }}",
                                authHomeUIState.resendingToken
                            )
                            kbController?.hide()
                        },
                        requestInProgressProvider = { authRequestUIState.requestInProgress },
                        exceptionMessageProvider = { authRequestUIState.requestExceptionMessage },
                        handleLocationPermissionRequest = targetActivity::handleLocationPermissionRequest,
                        snackbarIsDisplayingProvider = { snackbarUIState.isSnackbarDisplayingWhileRequestingAuthCode },
                        whenSnackbarIsDisplaying = {
                            targetActivity.authViewModel.updateSnackbar(
                                isSnackbarDisplayingWhileRequestingAuthCode = true
                            )
                        },
                        cancelPendingActiveListener = targetActivity.authViewModel::cancelPendingActiveListener,
                        onRetry = targetActivity.authViewModel::logUserOut,
                        snackbarHostState = scaffoldState.snackbarHostState
                    )
                }

                else -> {
                    var codeToVerify by rememberSaveable {
                        mutableStateOf("")
                    }

                    remember {
                        targetActivity.authViewModel.dismissSnackbar()
                        targetActivity.authViewModel.cancelPendingActiveListener()
                    }

                    VerifyCodeScreen(
                        codeToVerifyProvider = { codeToVerify },
                        onCodeChange = {
                            if (it.length <= VERIFICATION_CODE_LENGTH) {
                                if (hasException(authVerificationUIState.verificationExceptionMessage)) targetActivity.authViewModel.onVerificationException(
                                    ""
                                )

                                if (it.last().code != KEY_ENTER) // Not key Enter
                                    codeToVerify = it
                            }

                            if (codeToVerify.length == VERIFICATION_CODE_LENGTH) {
                                targetActivity.phoneAuth.onReceiveCodeToVerify(
                                    authHomeUIState.verificationId, codeToVerify
                                )
                                kbController?.hide()
                            }
                        },
                        exceptionMessageProvider = { authVerificationUIState.verificationExceptionMessage },
                        verificationInProgressProvider = { authVerificationUIState.verificationInProgress },
                        scaffoldState.snackbarHostState,
                        snackbarIsDisplayingProvider = { snackbarUIState.isSnackbarDisplayingWhileVerifyingAuthCode },
                        whenSnackbarIsDisplaying = {
                            targetActivity.authViewModel.updateSnackbar(
                                isSnackbarDisplayingWhileVerifyingAuthCode = true
                            )
                        },
                        onRetry = { targetActivity.authViewModel.logUserOut() },
                    )
                }
            }
        }
    }

    if (snackbarUIState.message.isNotEmpty()) {
        val kbController = LocalSoftwareKeyboardController.current
        kbController?.hide()
        LaunchedEffect(snackbarUIState.messageId) {
            scaffoldState.snackbarHostState.showSnackbar(
                snackbarUIState.message, duration = snackbarUIState.duration
            )
        }
    }
}

const val KEY_ENTER = 10

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoginWithPhoneNumberScreen(
    countryNamesAndDialCodes: List<CountryNamesAndDialCodes.NameAndDialCode>,
    selectedCountryProvider: () -> SelectedCountry,
    onSelectedCountryChange: (CountryNamesAndDialCodes.NameAndDialCode) -> Unit,
    phoneNumberProvider: () -> String,
    onPhoneNumberChange: (String) -> Unit,
    onDone: () -> Unit,
    requestInProgressProvider: () -> Boolean,
    exceptionMessageProvider: () -> String,
    handleLocationPermissionRequest: () -> Unit,
    snackbarIsDisplayingProvider: () -> Boolean,
    whenSnackbarIsDisplaying: () -> Unit,
    cancelPendingActiveListener: () -> Unit,
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

        Box {
            @Composable
            fun primaryComponent() {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(horizontalCenterColumnWidth)
                ) {
                    /*Image(
                        painter = painterResource(id = R.drawable.logo),
                        null,
                        modifier = Modifier.padding(top = 50.dp)
                    )*/

                    ForkedExposedDropdownMenuBox(expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier
                            .onFocusChanged {
                                if (!it.isFocused) if (selectedCountryName != selectedCountry.container.name) selectedCountryName =
                                    selectedCountry.container.name
                            }
                            .padding(top = 30.dp)

                    ) {
                        TextField(
                            value = selectedCountryName,
                            onValueChange = { selectedCountryName = it },
                            label = { Text(text = "Quốc gia") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expanded
                                )
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            singleLine = true,
                        )

                        val filter = countryNamesAndDialCodes.filter {
                            it.name.contains(
                                selectedCountryName, ignoreCase = true
                            )
                        }
                        if (filter.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                            ) {
                                LazyColumn {
                                    items(filter) {
                                        DropdownMenuItem(onClick = {
                                            selectedCountryName = it.name
                                            onSelectedCountryChange(it)
                                            expanded = false
                                        }) {
                                            Text(text = it.name)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        modifier = Modifier.padding(top = 18.dp),
                        onClick = handleLocationPermissionRequest
                    ) { Text(text = "Tự động xác định quốc gia từ vị trí") }

                    Row(modifier = Modifier.padding(top = 10.dp)) {
                        OutlinedTextField(
                            modifier = Modifier.layoutWithNewMaxWidth(with(LocalDensity.current) {
                                (horizontalCenterColumnWidth.toPx() * .44).toInt()
                            }),
                            value = selectedCountry.container.dialCode,
                            enabled = false,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(text = "Mã đt QG (*)") },
                        )

                        OutlinedTextField(
                            modifier = Modifier.layoutWithNewMaxWidth(with(LocalDensity.current) {
                                (horizontalCenterColumnWidth.toPx() * .56).toInt()
                            }),
                            value = phoneNumber,
                            onValueChange = onPhoneNumberChange,
                            singleLine = true,
                            label = {
                                Text(
                                    text = "Số điện thoại"
                                )
                            },
                            keyboardActions = KeyboardActions(onDone = { onDone() }),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done
                            ),
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                primaryComponent()
                if (hasException(exceptionMessage)) {
                    ExceptionShowBox(exceptionMessage = exceptionMessage)
                } else if (!requestInProgress) {
                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .width(horizontalCenterColumnWidth)
                            .padding(top = 24.dp)
                    ) { Text(text = "Xong") }
                }
            }

            if (requestInProgress) {
                val isSnackbarDisplaying = snackbarIsDisplayingProvider()
                if (isSnackbarDisplaying) {
                    LaunchedEffect(key1 = phoneNumber) {
                        showNoticeAndRecommendation(
                            snackbarHostState,
                            whenSnackbarIsDisplaying,
                            onRetry
                        )
                    }
                } else
                    LaunchedEffect(key1 = phoneNumber) {
                        delay(TIME_THRESHOLD_FOR_RESPONSE)
                        cancelPendingActiveListener()
                        showNoticeAndRecommendation(
                            snackbarHostState,
                            whenSnackbarIsDisplaying,
                            onRetry
                        )
                    }

                Surface(
                    color = Color.Transparent.copy(0.37f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    @Composable
                    fun subComponent() {
                        Surface(
                            modifier = Modifier
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

                    SubcomposeLayout { constraints ->
                        val placeables =
                            subcompose(0) { primaryComponent() }.map { it.measure(constraints) }

                        layout(constraints.maxWidth, constraints.maxHeight) {
                            subcompose(1) { subComponent() }.forEach {
                                it.measure(constraints).placeRelative(0, placeables[0].height)
                            }
                        }
                    }
                }
            }
        }
    }
}

const val TIME_THRESHOLD_FOR_RESPONSE = 40000L

suspend fun showNoticeAndRecommendation(
    snackbarHostState: SnackbarHostState,
    snackbarUIStateUpdate: () -> Unit,
    doAsRecommended: () -> Unit
) = coroutineScope {
    snackbarUIStateUpdate()
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
    snackbarIsDisplayingProvider: () -> Boolean,
    whenSnackbarIsDisplaying: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val codeToVerify = codeToVerifyProvider()
    val exceptionMessage = exceptionMessageProvider()
    val verificationInProgress = verificationInProgressProvider()

    Box {
        @Composable
        fun primaryComponent() {
            Column(
                modifier = modifier
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
        }
        primaryComponent()

        if (verificationInProgress) {
            val isSnackbarDisplaying = snackbarIsDisplayingProvider()
            if (isSnackbarDisplaying) {
                LaunchedEffect(key1 = codeToVerify) {
                    showNoticeAndRecommendation(
                        snackbarHostState,
                        whenSnackbarIsDisplaying,
                        onRetry
                    )
                }
            } else LaunchedEffect(key1 = codeToVerify) {
                delay(TIME_THRESHOLD_FOR_RESPONSE)
                showNoticeAndRecommendation(
                    snackbarHostState,
                    whenSnackbarIsDisplaying,
                    onRetry
                )
            }

            Surface(
                color = Color.Transparent.copy(0.37f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                @Composable
                fun subComponent() {
                    Surface(
                        modifier = Modifier
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

                SubcomposeLayout { constraints ->
                    val placeables =
                        subcompose(0) { primaryComponent() }.map { it.measure(constraints) }

                    layout(constraints.maxWidth, constraints.maxHeight) {
                        subcompose(1) { subComponent() }.forEach {
                            it.measure(constraints).placeRelative(0, placeables[0].height)
                        }
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
            listOf(),
            { SelectedCountry.getDefaultInstance() },
            {},
            { "" },
            {},
            {},
            { false },
            { "" },
            {},
            { false },
            {},
            {},
            {},
            SnackbarHostState(),
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

class NetworkCallbackExt(private val activity: AuthActivity) :
    ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        activity.isConnected = true
    }

    override fun onUnavailable() {
        super.onUnavailable()
        activity.isConnected = false
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        activity.isConnected = false
    }
}