package com.example.firebaseauth.data.local

import android.content.Context
import com.example.firebaseauth.data.CountryNameAndCallingCodeModelFromJSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import javax.inject.Inject

class SelectedCountryService @Inject constructor(context: Context) {
    private val savedFile = File(context.filesDir.path, "selected_country.json")

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun saveSelectedCountry(selectedCountry: CountryNameAndCallingCodeModelFromJSON) {
        withContext(Dispatchers.Default) {
            try {
                val jsonString =
                    json.encodeToString<CountryNameAndCallingCodeModelFromJSON>(selectedCountry)

                launch(context = Dispatchers.IO) {
                    with(BufferedWriter(FileWriter(savedFile))) {
                        write(jsonString)
                        close()
                    }
                }
            } catch (exc: Exception) {
                throw exc
            }
        }
    }

    suspend fun getSelectedCountry(): CountryNameAndCallingCodeModelFromJSON =
        withContext(Dispatchers.Default) {
            try {
                val buff = ByteArray(savedFile.length().toInt())
                launch(Dispatchers.IO) {
                    with(FileInputStream(savedFile)) {
                        read(buff)
                        close()
                    }
                }.join()

                val jsonString = String(buff)
                json.decodeFromString<CountryNameAndCallingCodeModelFromJSON>(
                    jsonString
                )
            } catch (exc: Exception) {
                throw exc
            }
        }
}