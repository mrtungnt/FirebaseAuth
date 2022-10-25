package com.example.firebaseauth.data

var increasement = 0

@kotlinx.serialization.Serializable()
data class CountryNamesAndCallingCodesModel(
    val name: String,
    val alpha2Code: String,
    val callingCodes: List<String>,
    val ordinal: Int = ++increasement,
    )
