package com.example.firebaseauth.data

@kotlinx.serialization.Serializable()
data class CountryModel(val name: String, val alpha2Code: String, val callingCodes: List<String>)
