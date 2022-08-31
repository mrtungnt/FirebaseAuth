package com.example.firebaseauth.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class CountryAndDialCodeModel(val name: String, val dial_code: String):Parcelable