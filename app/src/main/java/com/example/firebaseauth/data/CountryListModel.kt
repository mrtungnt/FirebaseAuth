package com.example.firebaseauth.data

import kotlinx.serialization.Serializable

@Serializable()
data class CountryListModel(
    private val error: Boolean,
    private val msg: String,
     val data: List<CountryModel>
)