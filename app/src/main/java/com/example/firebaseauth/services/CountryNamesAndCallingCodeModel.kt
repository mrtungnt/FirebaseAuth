package com.example.firebaseauth.services

@kotlinx.serialization.Serializable()
data class CountryNamesAndCallingCodeModel(
    val name: String,
    val alpha2Code: String,
    val callingCodes: List<String>,
    )
