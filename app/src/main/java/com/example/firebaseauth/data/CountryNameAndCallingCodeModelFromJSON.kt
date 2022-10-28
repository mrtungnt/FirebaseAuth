package com.example.firebaseauth.data

@kotlinx.serialization.Serializable()
data class CountryNameAndCallingCodeModelFromJSON(
    val name: String,
    val alpha2Code: String,
    val callingCodes: List<String>,
    )
