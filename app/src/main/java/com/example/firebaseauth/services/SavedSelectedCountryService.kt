package com.example.firebaseauth.services

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import javax.inject.Inject

class SavedSelectedCountryService @Inject constructor(context: Context) {
    private val savedFile = File(context.filesDir.path, "saved_selected_country.json")

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun saveSelectedCountry(selectedCountry: CountryNamesAndCallingCodeModel) {
        withContext(Dispatchers.Default) {
            try {
                val jsonString =
                    json.encodeToString<CountryNamesAndCallingCodeModel>(selectedCountry)

                with(BufferedWriter(FileWriter(savedFile))) {
                    write(jsonString)
                    close()
                }
            } catch (exc: Exception) {
                throw exc
            }
        }
    }

    suspend fun getSelectedCountry(): CountryNamesAndCallingCodeModel? =
        withContext(Dispatchers.Default) {
            try {
                val buff = ByteArray(savedFile.length().toInt())
                with(FileInputStream(savedFile)) {
                    read(buff)
                    close()
                }

                val jsonString = String(buff)
                json.decodeFromString<CountryNamesAndCallingCodeModel>(
                    jsonString
                )
            } catch (exc: Exception) {
                /*if (exc is FileNotFoundException)
                    null
                else*/
                    throw exc
            }
        }
}