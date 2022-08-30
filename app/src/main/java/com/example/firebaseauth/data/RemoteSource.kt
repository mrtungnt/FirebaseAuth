package com.example.firebaseauth.data

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RemoteSource @Inject constructor() {
    private val client = HttpClient(Android)

    suspend fun getCountriesAndDialCodes(): Result<CountryListModel> {
        val response = client.get("https://countriesnow.space/api/v0.1/countries/codes")
        val json = Json { ignoreUnknownKeys = true }
        val result: CountryListModel?
        try {
            result =
                json.decodeFromString<CountryListModel>(response.bodyAsText())
        } catch (e: Exception) {
            return Result.failure(RequestException("Failed to get countries and dial codes."))
        }
        return Result.success(result)
    }
}

class RequestException(private val msg: String) : Exception() {
    override val message: String
        get() = msg
}