package com.example.firebaseauth

import androidx.lifecycle.SavedStateHandle
import com.example.firebaseauth.data.CountryNamesAndDialCodesRepository
import com.example.firebaseauth.data.SavedSelectedCountryRepository
import com.example.firebaseauth.data.SavedSelectedCountryRepositoryImpl
import com.example.firebaseauth.di.ServiceBindersModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/*class FakeCountryNamesAndDialCodesRepositoryImpl @Inject constructor() :
    CountryNamesAndDialCodesRepository {
    override suspend fun getCountriesAndDialCodes(): Result<CountryNamesAndDialCodes> =
        coroutineScope {
            Result.success(CountryNamesAndDialCodes.getDefaultInstance())
        }
}*/

/*class FakeSavedSelectedCountryRepositoryImpl @Inject constructor() :
    SavedSelectedCountryRepository {
    override suspend fun saveSelectedCountry(selectedCountry: CountryNamesAndDialCodes.NameAndDialCode) {
        TODO("Not yet implemented")
    }

    override fun getFlowOfSelectedCountry(): Flow<SelectedCountry> {
        return flowOf(SelectedCountry.getDefaultInstance())
    }
}*/

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
                return flowOf(SelectedCountry.getDefaultInstance())
            }
        }
    }
}