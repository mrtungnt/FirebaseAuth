package com.example.firebaseauth.data

import kotlinx.serialization.Serializable

@Serializable()
data class CountryAndDialCodeAggregateModel(
    private val error: Boolean,
    private val msg: String,
     val data: List<CountryAndDialCodeModel>
)