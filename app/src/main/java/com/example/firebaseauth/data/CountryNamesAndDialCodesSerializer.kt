/*
package com.example.firebaseauth.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.firebaseauth.CountryNamesAndDialCodes
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object CountryNamesAndDialCodesSerializer : Serializer<CountryNamesAndDialCodes> {
    override val defaultValue: CountryNamesAndDialCodes = CountryNamesAndDialCodes.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): CountryNamesAndDialCodes {
        try {
            return CountryNamesAndDialCodes.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: CountryNamesAndDialCodes, output: OutputStream) = t.writeTo(output)
}
*/
