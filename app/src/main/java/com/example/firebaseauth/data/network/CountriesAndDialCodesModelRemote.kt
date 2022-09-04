package com.example.firebaseauth.data.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class CountryAndDialCodeModelRemote(val name: String, val dial_code: String) : Parcelable

@Serializable()
data class CountriesAndDialCodesModelRemote(
    private val error: Boolean,
    private val msg: String,
    val data: List<CountryAndDialCodeModelRemote>
)