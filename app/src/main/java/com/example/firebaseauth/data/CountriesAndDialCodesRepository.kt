package com.example.firebaseauth.data

import android.util.Log
import com.example.firebaseauth.CountriesAndDialCodes
import com.example.firebaseauth.data.local.CountriesAndDialCodesLocalSource
import com.example.firebaseauth.data.network.CountriesAndDialCodesModelRemote
import com.example.firebaseauth.data.network.CountriesAndDialCodesRemoteSource
import com.example.firebaseauth.data.network.CountryAndDialCodeModelRemote
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import javax.inject.Inject


class CountriesAndDialCodesRepository @Inject constructor(
    private val remoteSource: CountriesAndDialCodesRemoteSource,
    private val localSource: CountriesAndDialCodesLocalSource
) {
    suspend fun getCountriesAndDialCodes(): Result<CountriesAndDialCodes> {
        return when {
            localSource.getData().first().dataCount > 0 -> Result.success(
                localSource.getData().first()
            )
            else -> {
                val r = remoteSource.getCountriesAndDialCodes()
                if (r.isSuccess) {
                    val c: MutableCollection<CountriesAndDialCodes.CountryAndDialCode> =
                        mutableListOf()
                    r.getOrNull()?.data?.forEach {
                        c.add(
                            CountriesAndDialCodes.CountryAndDialCode.newBuilder().setName(it.name)
                                .setDialCode(it.dial_code).build()
                        )
                    }
                    localSource.dataStore.updateData { it.toBuilder().addAllData(c).build() }
                    Result.success(localSource.getData().last())
                } else
                    Result.failure(Exception(r.exceptionOrNull()))
            }
        }
    }
}