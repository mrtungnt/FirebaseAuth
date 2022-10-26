package com.example.firebaseauth.data

import com.example.firebaseauth.CountryNamesAndCallingCodes
import com.example.firebaseauth.CountryNamesAndDialCodes
import com.example.firebaseauth.data.local.CountryNamesAndCallingCodesService
import com.example.firebaseauth.data.local.CountryNamesAndDialCodesLocalSource
import com.example.firebaseauth.data.network.CountriesAndDialCodesRemoteSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface CountryNamesAndDialCodesRepository {
    suspend fun getCountriesAndDialCodes(): Result<CountryNamesAndCallingCodes>
}

class CountryNamesAndDialCodesRepositoryImpl @Inject constructor(
    private val countryNamesAndCallingCodesService: CountryNamesAndCallingCodesService
):CountryNamesAndDialCodesRepository {
    override suspend fun getCountriesAndDialCodes(): Result<CountryNamesAndCallingCodes> {
        countryNamesAndCallingCodesService.getCountryNamesAndCallingCodes()
        return when {
            localSource.getData().first().entriesCount > 0 -> Result.success(
                localSource.getData().first()
            )
            else -> {
                val r = remoteSource.getCountriesAndDialCodes()
                if (r.isSuccess) {
                    val c: MutableCollection<CountryNamesAndDialCodes.NameAndDialCode> =
                        mutableListOf()
                    r.getOrNull()?.data?.forEach {
                        c.add(
                            CountryNamesAndDialCodes.NameAndDialCode.newBuilder().setName(it.name)
                                .setDialCode(it.dial_code).build()
                        )
                    }
                    localSource.saveCountriesAndDialCodes(c)
                    Result.success(localSource.getData().first())
                } else
                    Result.failure(Exception(r.exceptionOrNull()))
            }
        }
    }
}
