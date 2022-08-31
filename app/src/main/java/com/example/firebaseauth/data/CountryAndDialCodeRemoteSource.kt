package com.example.firebaseauth.data

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CountryAndDialCodeRemoteSource @Inject constructor() {
    private val client = HttpClient(Android)

    suspend fun getCountriesAndDialCodes(): Result<CountryAndDialCodeAggregateModel> {
        val result: CountryAndDialCodeAggregateModel?
        try {
            val response = client.get("https://countriesnow.space/api/v0.1/countries/codes")
            val json = Json { ignoreUnknownKeys = true }
            result =
                json.decodeFromString<CountryAndDialCodeAggregateModel>(response.bodyAsText())
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(result)
    }
}