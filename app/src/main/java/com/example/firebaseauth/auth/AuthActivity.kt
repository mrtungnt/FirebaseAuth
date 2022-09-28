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
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                Log.d("ACCESS_FINE_LOCATION", "Granted: as requested")
                whenLocationPermissionGranted()
            }

            permissions.getOrDefault(
                Manifest.permission.ACCESS_COARSE_LOCATION, false
            ) -> {
                // Only approximate location access granted.
                Log.d("ACCESS_COARSE_LOCATION", "Granted: as requested")
                whenLocationPermissionGranted()
            }

            else -> {
                // No location access granted.
                Log.d("ACCESS_LOCATION", "Granted: NONE")
            }
        }
    }

    private val intentSenderForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                Log.d("intentSenderForResult", "Location setting: On now")
                whenLocationReady()
            } else
                Log.d("ResolutionForResult", "resultCode: ${activityResult.resultCode}")
        }

    fun handleLocationPermissionRequest() {
        val permissionCheck = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        when {
            permissionCheck == PackageManager.PERMISSION_GRANTED -> {
                Log.d(
                    "ACCESS_COARSE_LOCATION", "Granted: yes $permissionCheck"
                )
                whenLocationPermissionGranted()
            }

            shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                Log.d(
                    "ACCESS_COARSE_LOCATION", "ShouldShowRequestPermissionRationale: yes"
                )

                Toast.makeText(applicationContext, "ACCESS_COARSE_LOCATION needed", 1000)
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
                    location.latitude,
                    location.longitude,
                    1
                )
            } catch (exc: java.lang.Exception) {
                Log.e("Location", "${exc.message}")
                emptyList<Address>()
            }
        }
    }

    private fun whenLocationReady() {
        authViewModel.updateSnackbar(
            "Đang xác định quốc gia từ vị trí. Trong một số điều kiện, có thể mất 30 giây.",
            true
        )
        authViewModel.locationTask = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            authViewModel.locationCancellationToken
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
                Log.d("Location", "is null")
                authViewModel.updateSnackbar("Không xác định được vị trí.")
            }
        }
        authViewModel.locationTask?.addOnFailureListener {
            Log.e(
                "Location",
                "msg: ${it.message}"
            )
        }
    }

    private fun whenLocationPermissionGranted() {
        val locationSettingsRequestBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(LocationRequest.create())
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)
        val taskLocationSettingsResponse: Task<LocationSettingsResponse> =
            settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build())

        taskLocationSettingsResponse.addOnSuccessListener {
            whenLocationReady()
        }
        taskLocationSettingsResponse.addOnFailureListener {
            Log.e(
                "LocationSettingsResponse",
                "value: $it"
            )
            if (it is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    val status = it.status
                    if (status.hasResolution()) {
                        val pendingIntent: PendingIntent = status.resolution!!
//                        Preconditions.checkNotNull(pendingIntent)
                        intentSenderForResult.launch(
                            IntentSenderRequest.Builder(pendingIntent.intentSender)/*.setFillInIntent(null)
                                .setFlags(0, 0)*/.build()
                        )
                    }

                    /*it.startResolutionForResult(
                        this,
                        it.statusCode
                    )*/
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    Log.e(
                        "SendIntent",
                        "msg: ${it.message}"
                    )
                }
            }
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var isConnected by mutableStateOf(false)

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connMgr =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            NetworkCallbackExt(this)
        )

//        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            FirebaseAuthTheme {
                val scaffoldState = rememberScaffoldState()
                Scaffold(scaffoldState = scaffoldState)
                {
                    /*val authViewModel = viewModel<AuthViewModel>()
                    Log.d(
                        "authViewModel",
                        "Singleton: ${authViewModel === this@AuthActivity.viewModel.value}"
                    )*/
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

                            /*override fun onLoggingProgress(progress: Boolean) {
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

                    val authState by authViewModel.authStateFlow.collectAsState()


                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                    ) {
                        AuthHomeScreen(
                            phoneAuth,
                            authState,
                            this@AuthActivity,
                        )
                    }

                    if (authState.snackbarMsg.isNotEmpty()) {
                        val kbController = LocalSoftwareKeyboardController.current
                        kbController?.hide()
                        LaunchedEffect(authState.snackbarMsg) {
                            scaffoldState.snackbarHostState.showSnackbar(
                                authState.snackbarMsg,
                                duration = SnackbarDuration.Indefinite
                            )
                        }
                    } else scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthHomeScreen(
    phoneAuth: PhoneAuth,
    authState: AuthUIState,
    targetActivity: AuthActivity,
) {
    val savedSelectedCountryState by targetActivity.authViewModel.flowOfSavedSelectedCountry.collectAsState(
        initial = SelectedCountry.getDefaultInstance()
    )

    fun hasUserLoggedIn() = authState.userSignedIn

    when {
        targetActivity.authViewModel.connectionExceptionMessage.isNotEmpty() -> {
            Text(text = targetActivity.authViewModel.connectionExceptionMessage)
        }

        hasUserLoggedIn() -> {
            Column {
                val user = Firebase.auth.currentUser
                Text(text = "Welcome ${user?.displayName ?: user?.phoneNumber}")
                Button(onClick = targetActivity.authViewModel::logUserOut) {
                    Text(text = "Sign out")
                }
            }
        }

        targetActivity.authViewModel.countriesAndDialCodes.isEmpty() -> {
            Text("Khởi tạo")
        }

        else -> {
            when {
                authState.verificationId.isEmpty() -> {
                    var phoneNumber by rememberSaveable {
                        mutableStateOf("")
                    }

                    val kbController = LocalSoftwareKeyboardController.current

                    LoginWithPhoneNumberScreen(
                        countryNamesAndDialCodes = targetActivity.authViewModel.countriesAndDialCodes,
                        selectedCountry = savedSelectedCountryState,
                        onSelectedCountryChange = {
                            targetActivity.authViewModel.saveSelectedCountry(it)
                            if (hasException(authState.requestExceptionMessage)) targetActivity.authViewModel.onRequestException(
                                ""
                            )
                        },
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = {
                            if (hasException(authState.requestExceptionMessage)) targetActivity.authViewModel.onRequestException(
                                ""
                            )
                            phoneNumber = it
                        },
                        onDone = {
                            if (savedSelectedCountryState.container.dialCode.isEmpty()) targetActivity.authViewModel.onEmptyDialCode()
                            else phoneAuth.startPhoneNumberVerification(
                                "${savedSelectedCountryState.container.dialCode}${phoneNumber.trimStart { it == '0' }}",
                                authState.resendingToken
                            )
                            kbController?.hide()
                        },
                        requestInProgress = authState.requestInProgress,
                        exceptionMessage = authState.requestExceptionMessage,
                        handleLocationPermissionRequest = targetActivity::handleLocationPermissionRequest
                    )
                }

                else -> {
                    var codeToVerify by rememberSaveable {
                        mutableStateOf("")
                    }

                    remember {
                        targetActivity.authViewModel.clearSnackbar()
                        targetActivity.authViewModel.cancelPendingActiveListener()
                    }

                    VerifyCodeScreen(
                        codeToVerify = codeToVerify,
                        onCodeChange = {
                            if (it.length <= VERIFICATION_CODE_LENGTH) {
                                if (hasException(authState.verificationExceptionMessage)) targetActivity.authViewModel.onVerificationException(
                                    ""
                                )

                                if (it.last().code != 10) // Not key Enter
                                    codeToVerify = it
                            }

                            if (codeToVerify.length == VERIFICATION_CODE_LENGTH) {
                                phoneAuth.onReceiveCodeToVerify(
                                    authState.verificationId, codeToVerify
                                )
                            }
                        },
                        exceptionMessage = authState.verificationExceptionMessage,
                        verificationInProgress = authState.verificationInProgress,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoginWithPhoneNumberScreen(
    countryNamesAndDialCodes: List<CountryNamesAndDialCodes.NameAndDialCode>,
    selectedCountry: SelectedCountry,
    onSelectedCountryChange: (CountryNamesAndDialCodes.NameAndDialCode) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onDone: () -> Unit,
    requestInProgress: Boolean,
    exceptionMessage: String?,
    handleLocationPermissionRequest: () -> Unit
) {
    Surface(
        color = MaterialTheme.colors.background, modifier = Modifier.fillMaxWidth()
    ) {
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

            if (hasException(exceptionMessage)) {
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .wrapContentSize(),
                    color = MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.error,
                    elevation = 2.dp
                ) {
                    Text(
                        text = exceptionMessage!!,
                        Modifier.padding(10.dp),
                    )
                }
            } else if (requestInProgress) {
                var message by rememberSaveable {
                    mutableStateOf("Chờ mã xác minh")
                }
                LaunchedEffect(key1 = phoneNumber) {
                    delay(10000)
                    message = "Thời gian đợi hơi lâu."
                }
                Column(
                    Modifier
                        .padding(18.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
//                    Divider(Modifier.height(3.dp))
                    Text(
                        text = message,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            } else {
                val guidance = remember {
                    "(*) Người dùng không phải nhập trực tiếp mã điện thoại quốc gia, mà thông qua chọn quốc gia  phát hành thẻ sim dùng để đăng ký."
                }
                Column(modifier = Modifier.width(horizontalCenterColumnWidth)) {
                    Button(
                        onClick = onDone,
                        modifier = Modifier
//                            .width(horizontalCenterColumnWidth)
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                    ) { Text(text = "Xong") }

                    Text(
                        text = guidance,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.padding(top = 30.dp)
                    )
                }
            }
        }
    }
}

fun constraintsWithNewMaxWidth(constraints: Constraints, newMaxWith: Int): Constraints =
    Constraints(
        minWidth = constraints.minWidth,
        maxWidth = newMaxWith,
        minHeight = constraints.minHeight,
        maxHeight = constraints.maxHeight
    )


fun Modifier.layoutWithNewMaxWidth(newMaxWith: Int): Modifier = layout { measurable, constraints ->
    val component = measurable.measure(
        constraintsWithNewMaxWidth(constraints, newMaxWith)
    )
    layout(
        component.width, component.height
    ) { component.placeRelative(0, 0) }
}

/*
@Composable
fun PhoneNumberInputCombo(content: @Composable () -> Unit) {
    SubcomposeLayout { constraints ->
        var measurables = subcompose(1, content)
        var placeable1 = measurables[0].measure(constraints)
        val size = IntSize(placeable1.width, placeable1.height)

        measurables =
            subcompose(2, content) // subcomposing the same content requires another slotId

        placeable1 = measurables[0].measure(
            ConstraintsWithNewMaxWidth(constraints, (size.width * .4).toInt())
        )

        val placeable2 = measurables[1].measure(
            ConstraintsWithNewMaxWidth(constraints, (size.width * .6).toInt())
        )

        layout(size.width, size.height) {
            placeable1.placeRelative(0, 0)
            placeable2.placeRelative(placeable1.width, 0)
        }
    }
}
*/

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
        Spacer(
            modifier = Modifier.height(5.dp)
        )
        Column(
            modifier = modifier.width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { }, Modifier.fillMaxWidth()) {
                Text(text = "Xác thực bằng số điện thoại khác")
            }
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
            SelectedCountry.getDefaultInstance(),
            {},
            "",
            {},
            {},
            false,
            null,
            {}
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

class NetworkCallbackExt(private val activity: AuthActivity) :
    ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        activity.isConnected = true
    }
}