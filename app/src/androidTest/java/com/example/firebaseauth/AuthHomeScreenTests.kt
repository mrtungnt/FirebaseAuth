package com.example.firebaseauth

import com.example.firebaseauth.auth.AuthViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

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
        Assert.assertEquals(authViewModel.authState.authRequestUIState.requestExceptionMessage, "")
    }
}