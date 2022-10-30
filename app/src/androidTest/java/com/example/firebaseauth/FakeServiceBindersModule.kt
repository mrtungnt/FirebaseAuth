/*
package com.example.firebaseauth

import com.example.firebaseauth.repositories.CountryNamesAndDialCodesRepository
import com.example.firebaseauth.repositories.SavedSelectedCountryRepository
import com.example.firebaseauth.di.ServiceBindersModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ServiceBindersModule::class]
)
class FakeServiceProvidersModule {
    @Singleton
    @Provides
    fun providesCountryNamesAndDialCodesRepository(): CountryNamesAndDialCodesRepository {
        return object : CountryNamesAndDialCodesRepository {
            override suspend fun getCountriesAndDialCodes(): Result<CountryNamesAndDialCodes> =
                coroutineScope {
                    Result.success(CountryNamesAndDialCodes.getDefaultInstance())
                }
        }
    }

    @Singleton
    @Provides
    fun providesSavedSelectedCountryRepository(): SavedSelectedCountryRepository {
        return object : SavedSelectedCountryRepository {
            override suspend fun saveSelectedCountry(selectedCountry: CountryNamesAndDialCodes.NameAndDialCode) {
                TODO("Not yet implemented")
            }

            override fun getFlowOfSelectedCountry(): Flow<SelectedCountry> {
                return flowOf(
                    SelectedCountry.newBuilder().setContainer(
                        CountryNamesAndDialCodes.NameAndDialCode.newBuilder()
                            .setName("This country").setDialCode("+84").build()
                    ).build()
                )
            }
        }
    }
}*/
