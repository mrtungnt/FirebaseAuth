package com.example.firebaseauth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
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
import timber.log.Timber
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
        var isLandingScreenDone by mutableStateOf(false)

        var shouldShowLandingScreen by mutableStateOf(true)

        composeRule.setContent()
        {
            if (shouldShowLandingScreen) {
                LandingScreen(isDoneProvider = { isLandingScreenDone }) {
                    shouldShowLandingScreen = false
                }
            } else
                Text(text = "Landing screen is done.")
        }

        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(120)
        composeRule.onNodeWithTag("LogoAndSlogan").assertExists()
    }

    @Test
    fun landingScreenDoneTest() {
        var isLandingScreenDone by mutableStateOf(false)

        var shouldShowLandingScreen by mutableStateOf(true)

        composeRule.setContent()
        {
            if (shouldShowLandingScreen) {
                LandingScreen(isDoneProvider = { isLandingScreenDone }) {
                    shouldShowLandingScreen = false
                }

                LaunchedEffect(key1 = Unit) {
                    delay(1800)
                    isLandingScreenDone = true
                }
            } else
                Text(text = "Landing screen is done.")
        }

        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(3000)
        composeRule.onNodeWithTag("LogoAndSlogan").assertDoesNotExist()
    }

    @Test
    fun testControlClock() {
        var toggle by mutableStateOf(false)
        Timber.plant(Timber.DebugTree())
        composeRule.setContent {
            var count by remember { mutableStateOf(0) }
            if (!toggle) {
                DisposableEffect(Unit) {
                    count++
                    // Apply the change to `count` in the snapshot:
//                Snapshot.sendApplyNotifications()
                    // Note: we apply the snapshot manually here for illustration purposes. In general
                    // we recommended against doing this in production code.
                    onDispose {
                        println("Disposed - count: $count")
                        Timber.tag("clocktest").d("Disposed - count: $count")
                    }
                }
                Text("Effect ran $count time(s), toggle is $toggle")
            } else {
                Text("Changed in composition, count now is: $count")
                count--
            }
        }

        // Check initial state
        composeRule.onNodeWithText("Effect ran 1 time(s), toggle is false").assertExists()
        // Take control of the clock
        composeRule.mainClock.autoAdvance = false

        // Change the `toggle` state variable
        toggle = true
        // Apply the change to `toggle` in the snapshot:
//        Snapshot.sendApplyNotifications()

        // Recomposition hasn't yet happened:
        composeRule.onNodeWithText("Effect ran 1 time(s), toggle is false").assertExists()
        // Forward the clock by 2 frames: 1 for `toggle` and then 1 for `count`
        composeRule.mainClock.advanceTimeBy(16)
        // UI now fully reflects the new state
        try {
            composeRule.onNodeWithText("Changed in composition, count now is: 0").assertExists()
        } catch (exc: java.lang.AssertionError) {
//            println(exc.message)
            Timber.d( exc?.message!!)
            composeRule.onRoot().printToLog("clocktest")
            throw exc
        }
    }
}
