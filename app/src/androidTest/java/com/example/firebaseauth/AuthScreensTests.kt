package com.example.firebaseauth

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.firebaseauth.auth.AuthActivity
import com.example.firebaseauth.ui.LoginWithPhoneNumberScreen
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class AuthScreensTests {

    @get:Rule
    val composeRule = createAndroidComposeRule<AuthActivity>()
    private val snackbarHostState = SnackbarHostState()

    @Test
    fun requestTimeout_snackbarShown() {
        val expectedSnackbarText = "Thời gian phản hồi lâu hơn dự kiến."

        composeRule.setContent {
            LoginWithPhoneNumberScreen(
                countryNamesAndDialCodes = emptyList(),
                selectedCountryProvider = {
                    SelectedCountry.getDefaultInstance()
                },
                onSelectedCountryChange = {},
                phoneNumberProvider = { "" },
                onPhoneNumberChange = { /*TODO*/ },
                onDone = { /*TODO*/ },
                requestInProgressProvider = { true },
                exceptionMessageProvider = { "" },
                handleLocationPermissionRequest = { /*TODO*/ },
                isRequestTimeoutProvider = { true },
                onRequestTimeout = { /*TODO*/ },
                onRetry = { /*TODO*/ },
                snackbarHostState = snackbarHostState
            )
        }

        runBlocking {
            val actualSnackbarText =
                snapshotFlow { snackbarHostState.currentSnackbarData }.filterNotNull()
                    .first()?.message
            Assert.assertEquals(expectedSnackbarText, actualSnackbarText)
        }
    }

    /*@Test
    fun dismissSnack(){
        val snackbarHostState = SnackbarHostState()
val authViewModel = AuthViewModel(countryNamesAndDialCodesRepository = CountryNamesAndDialCodesRepository(remoteSource = CountriesAndDialCodesRemoteSource(), localSource = CountryNamesAndDialCodesLocalSource(dataStore = DataStoreFactory.)), )
        composeRule.setContent{
        val scaffoldState =
            rememberScaffoldState(snackbarHostState = authViewModel.snackbarHostState)
            AuthHomeScreen(scaffoldState = rememberScaffoldState(), authUIStateFlow = , targetActivity = )
        }
    }*/
}
