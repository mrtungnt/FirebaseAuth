package com.example.firebaseauth.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.firebaseauth.CountriesAndDialCodes
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object CountriesAndDialCodesSerializer : Serializer<CountriesAndDialCodes> {
    override val defaultValue: CountriesAndDialCodes = CountriesAndDialCodes.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): CountriesAndDialCodes {
        try {
            return CountriesAndDialCodes.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: CountriesAndDialCodes, output: OutputStream) = t.writeTo(output)
}
