/*
package com.example.firebaseauth.data.network

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CountriesAndDialCodesRemoteSource @Inject constructor() {
    private val client = HttpClient(Android)

    suspend fun getCountriesAndDialCodes(): Result<CountriesAndDialCodesModelRemote> {
        val result: CountriesAndDialCodesModelRemote?
        try {
            val response = client.get("https://countriesnow.space/api/v0.1/countries/codes")
            val json = Json { ignoreUnknownKeys = true }
            result =
                json.decodeFromString<CountriesAndDialCodesModelRemote>(response.bodyAsText())
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(result)
    }
}*/
