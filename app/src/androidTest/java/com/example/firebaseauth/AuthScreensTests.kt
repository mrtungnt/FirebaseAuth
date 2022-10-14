package com.example.firebaseauth

import android.app.Application
import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.runner.AndroidJUnitRunner
import com.example.firebaseauth.auth.AuthActivity
import com.example.firebaseauth.auth.AuthUIState
import com.example.firebaseauth.di.AuthHomeUIStateModule
import com.example.firebaseauth.ui.AuthHomeScreen
import com.example.firebaseauth.ui.LandingScreen
import com.example.firebaseauth.ui.LoginWithPhoneNumberScreen
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Singleton

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}

@HiltAndroidTest
@UninstallModules(AuthHomeUIStateModule::class)
class WithActivityTests {
    @Module
    @InstallIn(SingletonComponent::class)
    class FakeAuthHomeUIStateModule {
        @Singleton
        @Provides
        fun providesAuthHomeUI(): AuthUIState.AuthHomeUIState = AuthUIState.AuthHomeUIState(
            "", PhoneAuthProvider.ForceResendingToken.zza(),
            Firebase.auth.currentUser != null,
            false,
        )
    }

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var androidComposedRule = createAndroidComposeRule<AuthActivity>()

    @Before
    fun init() {
        hiltRule.inject()
        androidComposedRule.activity.setContent {
            val scaffoldState = rememberScaffoldState()
            AuthHomeScreen(
                scaffoldState = scaffoldState,
                targetActivity = androidComposedRule.activity
            )
        }
    }

    @Test
    fun test() {
        androidComposedRule.onNodeWithText("Xong").performClick()
        runBlocking { delay(6000) }
    }

    @Test
    fun dismissSnackbarTest() {

    }
}


class WithoutActivityTests {
    @get:Rule
    val composeRule = createComposeRule()
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
                    .first().message
            Assert.assertEquals(expectedSnackbarText, actualSnackbarText)
        }
    }

    @Test
    fun landingScreenTest() {
        composeRule.setContent()
        {
            var isLandingScreenDone by remember {
                mutableStateOf(false)
            }

            var shouldShowLandingScreen by remember {
                mutableStateOf(true)
            }
            if (shouldShowLandingScreen) {
                LandingScreen(isDoneProvider = { isLandingScreenDone }) {
                    shouldShowLandingScreen = false
                }
            } else
                Text(text = "Landing screen is done.")
        }

//        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(120)
        composeRule.onNodeWithTag("LogoAndSlogan").assertExists()
    }

    @Test
    fun landingScreenDoneTest() {
        composeRule.setContent()
        {
            var isLandingScreenDone by remember {
                mutableStateOf(false)
            }

            var shouldShowLandingScreen by remember {
                mutableStateOf(true)
            }
            if (shouldShowLandingScreen) {
                LandingScreen(isDoneProvider = { isLandingScreenDone }) {
                    shouldShowLandingScreen = false
                }

                LaunchedEffect(key1 = Unit) {
                    delay(1000)
                    isLandingScreenDone = true
                }
            } else
                Text(text = "Landing screen is done.")
        }

//        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(1500)
        composeRule.onNodeWithTag("LogoAndSlogan").assertDoesNotExist()
    }
}
