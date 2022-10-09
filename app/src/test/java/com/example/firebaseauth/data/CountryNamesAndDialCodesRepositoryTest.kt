package com.example.firebaseauth.data

import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Test

internal class CountryNamesAndDialCodesRepositoryTest {
    class FakeCountriesAndDialCodesRemoteSource {
        suspend fun getCountriesAndDialCodes(): Int {
            delay(100)
            return 100
        }
    }

    @Test
    fun go() {
        CoroutineScope(Dispatchers.Default).launch {
            val r = withContext(Dispatchers.Default) {
                FakeCountriesAndDialCodesRemoteSource().getCountriesAndDialCodes()
            }
            assertEquals(100,r)
        }
    }
}