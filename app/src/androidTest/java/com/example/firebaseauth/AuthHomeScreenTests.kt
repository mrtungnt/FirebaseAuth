package com.example.firebaseauth

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.example.firebaseauth.auth.AuthViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

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
class AuthHomeScreenTests {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var authViewModel: AuthViewModel

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun test() {
        Assert.assertEquals(authViewModel.authUIState.authRequestUIState.requestExceptionMessage, "")
    }
}