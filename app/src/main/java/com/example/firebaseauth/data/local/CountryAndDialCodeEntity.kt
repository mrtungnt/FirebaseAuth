package com.example.firebaseauth.data.local

import androidx.room.Entity

@Entity(tableName = "CountriesAndDialCodes")
data class CountryAndDialCodeEntity (val name: String, val dial_code: String)